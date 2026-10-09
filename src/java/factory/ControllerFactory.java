package factory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import context.ControllerContext;
import utilitaire.MappingMethodClass;

public class ControllerFactory {
        private final Map<Class<?>, Object> controllerCache = new HashMap<>();

        public Object getController(Class<?> controllerClass) throws Exception {

                Object controller = controllerCache.get(controllerClass);

                if (controller == null) {

                        controller = controllerClass
                                        .getDeclaredConstructor()
                                        .newInstance();

                        controllerCache.put(controllerClass, controller);
                }

                return controller;
        }

        public ControllerContext create(MappingMethodClass mapping) throws Exception {

                Class<?> controllerClass = Class.forName(mapping.getClassName());

                Object controller = getController(controllerClass);

                Method method = mapping.getMethod();

                ControllerContext context = new ControllerContext();

                context.setControllerClass(controllerClass);
                context.setControllerInstance(controller);
                context.setMethod(method);

                return context;
        }

}
