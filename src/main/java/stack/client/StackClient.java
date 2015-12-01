package stack.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * StackClient only retrieves the endpoint; it has no concept of the resource at the endpoint. That must be retrieved with getClient.
 * TODO: make this better and/or better rename; should just directly use the resource as the input, not just endpoint
 */
public class StackClient {

    private static final Logger log = LoggerFactory.getLogger(StackClient.class);

    private static final ObjectMapper om = new ObjectMapper();

    private final String protocol;
    private final String endpoint;
    private final int port;

    /**
     * @param protocol
     * @param endpoint
     * @param port
     */
    public StackClient(final String protocol, final String endpoint, final int port) {
        this.protocol = protocol;
        this.endpoint = endpoint;
        this.port = port;
    }

    public StackClient(final URL url) {
        this(url.getProtocol(), url.getHost(), url.getPort());
    }

    public <T> T getClient(final Class<T> resourceClass) {
        final Path resourcePathAnnotation = resourceClass.getDeclaredAnnotation(Path.class);
        Preconditions.checkNotNull(resourcePathAnnotation,
                "Class is not annotated with @Path. Only Classes with @Path annotation may generate a client without explicit endpoint.");

        // TODO: should actually check for "api" or whatever we've specified
        // TODO: note that "/" are not required by jersey
        final String resource = resourcePathAnnotation.value();

        final String url = String.format("%s://%s:%s/%s", protocol, endpoint, port, resource);
        return getClient(resourceClass, url);
    }

    public <T> T getClient(final Class<T> resourceClass, final String url) {
        // TODO: verify the url with the protocol, endpoint, and port

        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(resourceClass);
        factory.setFilter(method -> Modifier.isAbstract(method.getModifiers()));

        final MethodHandler handler = (object, thisMethod, proceed, args) -> {

            // determine the type of the request
            final boolean isPost = thisMethod.getAnnotation(POST.class) != null;
            final boolean isGet = thisMethod.getAnnotation(GET.class) != null;
            final boolean isPut = thisMethod.getAnnotation(PUT.class) != null;
            final boolean isDelete = thisMethod.getAnnotation(DELETE.class) != null;

            // TODO: only @POST, @GET, @PUT, @DELETE are supported for now
            if (!(isPost || isGet || isPut || isDelete)) {
                throw new IllegalStateException(thisMethod.getName()
                        + " must have exactly one from @POST, @GET, @PUT, @DELETE. Other operations are currently unsupported.");
            }

            // determine the uri of the resource
            final String unformattedPath = getUnformattedPath(thisMethod, url);
            final String formattedPath = getFormattedPath(unformattedPath, thisMethod, args);
            final URI uri = new URI(formattedPath).normalize();

            // TODO: once we introduce more parameter types, we can't mark out
            // args here we need to think of a way to do this better, most
            // likely an inner class is needed determine the entity, if any.
            // this just finds the last non-annotated argument and we will use
            // it as the entity to be consumed
            final Set<Object> remainingParameters = Arrays.asList(args).stream().filter(arg -> arg != Void.TYPE)
                    .collect(Collectors.toSet());
            Preconditions.checkState(remainingParameters.size() == 0 || remainingParameters.size() == 1,
                    "Too many non-annotated arguments: " + remainingParameters);
            final Object entityObject;
            if (remainingParameters.size() == 1) {
                entityObject = Iterables.getOnlyElement(remainingParameters);
            } else {
                entityObject = Void.TYPE;
            }

            // build the request
            final MediaType[] consumesType = getConsumesType(thisMethod);
            final MediaType[] producesType = getProducesType(thisMethod);
            final WebTarget client = ClientBuilder.newClient().target(uri);
            final Builder builder = client.request(producesType).accept(consumesType);

            final MediaType firstConsumesType;
            if (consumesType.length == 0) {
                log.debug("No valid @Consumes value found; defaulting to " + MediaType.APPLICATION_JSON_TYPE);
                firstConsumesType = MediaType.APPLICATION_JSON_TYPE;
            } else {
                if (consumesType.length > 1) {
                    log.debug("Found @Consumes " + Arrays.asList(consumesType) + " values; defaulting to " + consumesType[0]);
                }
                firstConsumesType = consumesType[0];
            }

            // make the request
            if (isPost) {
                return builder.post(Entity.entity(entityObject, firstConsumesType), thisMethod.getReturnType());
            } else if (isGet) {
                return builder.get(thisMethod.getReturnType());
            } else if (isPut) {
                return builder.put(Entity.entity(entityObject, firstConsumesType), thisMethod.getReturnType());
            } else if (isDelete) {
                return builder.delete(thisMethod.getReturnType());
            }

            throw new IllegalStateException("Could not make a valid request from method " + thisMethod);
        };

        try {
            final T resourceInstance = resourceClass.cast(factory.create(new Class<?>[0], new Object[0], handler));
            return resourceInstance;
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUnformattedPath(final Method thisMethod, final String url) {
        final Path methodPathAnnotation = thisMethod.getAnnotation(Path.class);
        final String unformattedPath;
        if (methodPathAnnotation != null) {
            final String methodPathValue = methodPathAnnotation.value();
            unformattedPath = String.format("%s/%s", url, methodPathValue);
        } else {
            unformattedPath = url;
        }
        return unformattedPath;
    }

    private String getFormattedPath(final String unformattedPath, final Method thisMethod, final Object[] args)
            throws Throwable {
        // TODO: need to support: @MatrixParam, @HeaderParam, @CookieParam, @FormParam
        // let's focus on path and query for now
        // TODO: need to support @Context

        // figure out what each parameter is for
        final Map<String, String> pathParameters = new HashMap<>();
        final Map<String, Object> queryParameters = new HashMap<>();

        // TODO: need to support: non-String types (mapped to String). use om
        // https://jersey.java.net/apidocs/2.22/jersey/javax/ws/rs/PathParam.html
        final Annotation[][] allParameterAnnotations = thisMethod.getParameterAnnotations();
        for (int i = 0; i < args.length; i++) {
            final Object argument = args[i];
            final List<Annotation> parameterAnnotations = Arrays.asList(allParameterAnnotations[i]);
            for (final Annotation parameterAnnotation : parameterAnnotations) {
                if (parameterAnnotation instanceof PathParam) {
                    final PathParam pathParam = PathParam.class.cast(parameterAnnotation);
                    final String encodedKey = URLEncoder.encode(pathParam.value(), "UTF-8");
                    final String encodedValue = URLEncoder.encode(argument.toString(), "UTF-8");
                    pathParameters.put(encodedKey, encodedValue);
                    args[i] = Void.TYPE;
                } else if (parameterAnnotation instanceof QueryParam) {
                    final QueryParam queryParam = QueryParam.class.cast(parameterAnnotation);
                    final String encodedKey = URLEncoder.encode(queryParam.value(), "UTF-8");

                    if (argument instanceof Set | argument instanceof List) {
                        queryParameters.put(encodedKey, argument);
                    } else {
                        final String encodedValue = URLEncoder.encode(argument.toString(), "UTF-8");
                        queryParameters.put(encodedKey, encodedValue);
                    }

                    args[i] = Void.TYPE;
                }
            }
        }

        String formattedPath = unformattedPath;
        for (final Entry<String, String> pathParameterMapping : pathParameters.entrySet()) {
            formattedPath = formattedPath.replaceAll("\\{" + pathParameterMapping.getKey() + "\\}",
                    pathParameterMapping.getValue());
        }

        if (queryParameters.size() > 0) {
            formattedPath += "?";
        }
        for (final Entry<String, Object> queryParameterMapping : queryParameters.entrySet()) {
            final Object value = queryParameterMapping.getValue();
            if (value instanceof List || value instanceof Set) {
                final Collection<?> list = (Collection) value;
                for (final Object param : list) {
                    formattedPath += queryParameterMapping.getKey() + "=" + param + "&";
                }
            } else {
                formattedPath += queryParameterMapping.getKey() + "=" + value + "&";
            }
        }
        return formattedPath;
    }

    private MediaType[] getConsumesType(final Method thisMethod) {
        final Consumes consumesAnnotation = thisMethod.getAnnotation(Consumes.class);
        if (consumesAnnotation != null) {
            final MediaType[] consumesType = getMediaTypes(consumesAnnotation);
            if (consumesType.length != 0) {
                return consumesType;
            }
        }

        final Class<?> parentClass = thisMethod.getDeclaringClass();
        log.debug("Failed to find @Consumes on " + thisMethod + "; searching @Consumes in " + thisMethod.getDeclaringClass());
        final Consumes parentConsumesAnnotation = parentClass.getAnnotation(Consumes.class);
        if (parentConsumesAnnotation != null) {
            final MediaType[] consumesType = getMediaTypes(parentConsumesAnnotation);
            if (consumesType.length != 0) {
                return consumesType;
            }
        }

        log.debug("No MediaType found @Consumes in " + thisMethod.getName() + " nor " + parentClass + "; ignoring annotation");
        return new MediaType[0];
    }

    private MediaType[] getMediaTypes(final Consumes consumesAnnotation) {
        final String[] consumesTypes = consumesAnnotation.value();
        final MediaType[] consumesType;
        if (consumesTypes.length == 0) {
            consumesType = new MediaType[0];
        } else {
            consumesType = Arrays.asList(consumesTypes).stream().map(MediaType::valueOf).collect(Collectors.toList()).toArray(new MediaType[0]);
        }
        return consumesType;
    }

    private MediaType[] getProducesType(final Method thisMethod) {
        final Produces producesAnnotation = thisMethod.getAnnotation(Produces.class);
        if (producesAnnotation != null) {
            final MediaType[] consumesType = getMediaTypes(producesAnnotation);
            if (consumesType.length != 0) {
                return consumesType;
            }
        }

        final Class<?> parentClass = thisMethod.getDeclaringClass();
        log.debug("Failed to find @Produces on " + thisMethod + "; searching @Produces in " + thisMethod.getDeclaringClass());
        final Produces parentProducesAnnotation = parentClass.getAnnotation(Produces.class);
        if (parentProducesAnnotation != null) {
            final MediaType[] consumesType = getMediaTypes(parentProducesAnnotation);
            if (consumesType.length != 0) {
                return consumesType;
            }
        }

        log.debug("No MediaType found for @Produces in " + thisMethod.getName() + " nor " + parentClass + "; ignoring annotation");
        return new MediaType[0];
    }

    private MediaType[] getMediaTypes(final Produces producesAnnotation) {
        final String[] consumesTypes = producesAnnotation.value();
        final MediaType[] consumesType;
        if (consumesTypes.length == 0) {
            consumesType = new MediaType[0];
        } else {
            consumesType = Arrays.asList(consumesTypes).stream().map(MediaType::valueOf).collect(Collectors.toList()).toArray(new MediaType[0]);
        }
        return consumesType;
    }
}
