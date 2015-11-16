package stack.client;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class StackClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(StackClient.class);

    private static final ObjectMapper om = new ObjectMapper();

    private final String protocol;
    private final String endpoint;
    private final int port;

    /**
     * 
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
        factory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method method) {
                return Modifier.isAbstract(method.getModifiers());
            }
        });

        final MethodHandler handler = new MethodHandler() {
            @Override
            public Object invoke(Object b, Method thisMethod, Method proceed, Object[] args) throws Throwable {

                // figure out the unformatted resource path of the request
                final Path methodPathAnnotation = thisMethod.getAnnotation(Path.class);
                final String unformattedPath;
                if (methodPathAnnotation != null) {
                    final String methodPathValue = methodPathAnnotation.value();
                    unformattedPath = String.format("%s/%s", url, methodPathValue);
                } else {
                    unformattedPath = url;
                }

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

                // figure out what the request consumes
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

                // figure out the request produces
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

                // if we know what to do with the parameter, remove it
                // after removing all known parameters, the last one is the
                // entity (if it exists)
                final Set<Integer> consumedParameters = new HashSet<>();
                for (int i = 0; i < args.length; i++) {
                    consumedParameters.add(i);
                }

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
                            consumedParameters.remove(i);
                        } else if (parameterAnnotation instanceof QueryParam) {
                            final QueryParam queryParam = QueryParam.class.cast(parameterAnnotation);
                            final String encodedKey = URLEncoder.encode(queryParam.value(), "UTF-8");
                            final String encodedValue = URLEncoder.encode(argument.toString(), "UTF-8");
                            queryParameters.put(encodedKey, encodedValue);
                            consumedParameters.remove(i);
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

                final URI uri = new URI(formattedPath).normalize();

                // figure out the entity, if any
                Preconditions.checkState(consumedParameters.size() == 0 || consumedParameters.size() == 1,
                        "Too many non-annotated arguments: " + consumedParameters.stream()
                                .map(m -> thisMethod.getParameterTypes()[m]).collect(Collectors.toSet()));
                final Object entityObject;
                if (consumedParameters.size() == 1) {
                    entityObject = args[Iterables.getOnlyElement(consumedParameters)];
                } else {
                    entityObject = "";
                }

                // build the request
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
            }
        };

        try {
            final T resourceInstance = resourceClass.cast(factory.create(new Class<?>[0], new Object[0], handler));
            return resourceInstance;
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        // TODO: to be implemented
    }
}
