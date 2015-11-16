package stack.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
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
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;


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

    public <T> T getClient(final Class<T> resourceClass) {
        final Path resourcePathAnnotation = resourceClass.getDeclaredAnnotation(Path.class);
        Preconditions.checkNotNull(resourcePathAnnotation,
                "Class is not annotated with @Path. Only Classes with @Path annotation may generated a client.");

        // TODO: should actually check for "api" or whatever we've specified
        // TODO: note that "/" are not required by jersey
        final String resource = resourcePathAnnotation.value();

        final String url = String.format("%s://%s:%s/%s", protocol, endpoint, port, resource);

        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(resourceClass);
        factory.setFilter(method -> Modifier.isAbstract(method.getModifiers()));

        final MethodHandler handler = (object, thisMethod, proceed, args) -> {

            // figure out what type of request this is
            final boolean isPost = thisMethod.getAnnotation(POST.class) != null;
            final boolean isGet = thisMethod.getAnnotation(GET.class) != null;
            final boolean isPut = thisMethod.getAnnotation(PUT.class) != null;
            final boolean isDelete = thisMethod.getAnnotation(DELETE.class) != null;

            // TODO: only @POST, @GET, @PUT, @DELETE are supported for now
            if (!(isPost || isGet || isPut || isDelete)) {
                throw new IllegalStateException(thisMethod.getName()
                        + " must have exactly one from @POST, @GET, @PUT, @DELETE. Other operations are currently unsupported.");
            }

            // figure out the unformatted resource path of the request
            final String unformattedPath = getUnformattedPath(thisMethod, url);
            final String formattedPath = getFormattedPath(unformattedPath, thisMethod, args);
            final URI uri = new URI(formattedPath).normalize();

            final Set<Object> remainingParameters = Arrays.asList(args).stream().filter(arg -> arg != null).collect(Collectors.toSet());

            // figure out the entity, if any
            Preconditions.checkState(remainingParameters.size() == 0 || remainingParameters.size() == 1,
                    "Too many non-annotated arguments: " + remainingParameters);
            final Object entityObject;
            if (remainingParameters.size() == 1) {
                entityObject = Iterables.getOnlyElement(remainingParameters);
            } else {
                entityObject = "";
            }

            // build the request
            final MediaType consumesType = getConsumesType(thisMethod);
            final MediaType producesType = getProducesType(thisMethod);
            final WebTarget client = ClientBuilder.newClient().target(uri);
            Builder builder;
            if (producesType != null) {
                builder = client.request(producesType);
            } else {
                builder = client.request();
            }

            if (consumesType != null) {
                builder = builder.accept(consumesType);
            }

            if (isPost) {
                return builder.post(Entity.entity(entityObject, consumesType), thisMethod.getReturnType());
            } else if (isGet) {
                return builder.get(thisMethod.getReturnType());
            } else if (isPut) {
                return builder.put(Entity.entity(entityObject, consumesType), thisMethod.getReturnType());
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

    private String getFormattedPath(final String unformattedPath, final Method thisMethod, final Object[] args) throws Throwable {
        // TODO: need to support: @MatrixParam, @HeaderParam,
        // @CookieParam, @FormParam
        // let's focus on path and query for now

        // figure out what each parameter is for
        final Map<String, String> pathParameters = new HashMap<>();
        final Map<String, String> queryParameters = new HashMap<>();

        // TODO: need to support: non-String types (mapped to String)
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
                    args[i] = null;
                } else if (parameterAnnotation instanceof QueryParam) {
                    final QueryParam queryParam = QueryParam.class.cast(parameterAnnotation);
                    final String encodedKey = URLEncoder.encode(queryParam.value(), "UTF-8");
                    final String encodedValue = URLEncoder.encode(argument.toString(), "UTF-8");
                    queryParameters.put(encodedKey, encodedValue);
                    args[i] = null;
                }
            }
        }

        // TODO: to be implemented:
        String formattedPath = unformattedPath;
        for (final Entry<String, String> pathParameterMapping : pathParameters.entrySet()) {
            formattedPath = formattedPath.replaceAll("\\{" + pathParameterMapping.getKey() + "\\}",
                    pathParameterMapping.getValue());
        }

        if (queryParameters.size() > 0) {
            formattedPath += "?";
        }
        for (final Entry<String, String> queryParameterMapping : queryParameters.entrySet()) {
            formattedPath += queryParameterMapping.getKey() + "=" + queryParameterMapping.getValue() + "&";
        }
        return formattedPath;
    }

    private MediaType getConsumesType(Method thisMethod) {
        final Consumes consumesAnnotation = thisMethod.getAnnotation(Consumes.class);
        final MediaType consumesType;
        if (consumesAnnotation != null) {
            final String[] consumesTypes = consumesAnnotation.value();
            if (consumesTypes.length == 0) {
                log.warn("No MediaType specified in @Consumes, using " + MediaType.WILDCARD_TYPE);
                consumesType = MediaType.WILDCARD_TYPE;
            } else {
                if (consumesTypes.length > 1) {
                    log.warn("Only one @Consume MediaType is supported. Using " + consumesTypes[0]);
                }
                consumesType = MediaType.valueOf(consumesTypes[0]);
            }
        } else {
            consumesType = MediaType.WILDCARD_TYPE;
        }
        return consumesType;
    }

    private MediaType getProducesType(Method thisMethod) {
        final Produces producesAnnotation = thisMethod.getAnnotation(Produces.class);
        final MediaType producesType;
        if (producesAnnotation != null) {
            final String[] producesTypes = producesAnnotation.value();
            if (producesTypes.length == 0) {
                log.warn("No MediaType specified in @Produces, using " + MediaType.WILDCARD_TYPE);
                producesType = MediaType.WILDCARD_TYPE;
            } else {
                if (producesTypes.length > 1) {
                    log.warn("Only one @Produces MediaType is supported. Using " + producesTypes[0]);
                }
                producesType = MediaType.valueOf(producesTypes[0]);
            }
        } else {
            producesType = MediaType.WILDCARD_TYPE;
        }
        return producesType;
    }
}
