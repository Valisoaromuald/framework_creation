package binder;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import context.ControllerContext;
import jakarta.servlet.http.HttpServletRequest;
import utilitaire.ClasseUtilitaire;
import utilitaire.MappingMethodClass;

public class ParameterBinder {

    public Object[] bind(
            ControllerContext context,
            Path uploadFolder,
            Map.Entry<String, MappingMethodClass> map,
            HttpServletRequest request,
            String url,
            List<String> classesNames) throws Exception {

        Object[] arguments = ClasseUtilitaire.giveMethodParameters(
                context.getControllerInstance(),
                uploadFolder,
                map,
                request,
                url,
                classesNames);

        context.setArguments(arguments);

        return arguments;
    }

}