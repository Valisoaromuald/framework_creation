package binder;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import context.ControllerContext;
import context.FrameworkContext;
import jakarta.servlet.http.HttpServletRequest;
import utilitaire.ClasseUtilitaire;
import utilitaire.MappingMethodClass;

public class ParameterBinder {

    public Object[] bind(
            ControllerContext context,
            FrameworkContext frameworkContext,
            Map.Entry<String, MappingMethodClass> map,
            HttpServletRequest request,
            String url) throws Exception {

        Object[] arguments = ClasseUtilitaire.giveMethodParameters(
                context.getControllerInstance(),
                frameworkContext.getUploadFolder(),
                map,
                request,
                url,
                frameworkContext.getClassNames());

        context.setArguments(arguments);

        return arguments;
    }

}