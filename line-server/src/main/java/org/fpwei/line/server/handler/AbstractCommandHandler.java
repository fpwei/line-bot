package org.fpwei.line.server.handler;

import com.linecorp.bot.model.message.Message;
import org.apache.commons.lang3.StringUtils;
import org.fpwei.line.core.common.CommonRuntimeException;
import org.fpwei.line.server.enums.Parameter;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCommandHandler implements Command {

    @Override
    public Message execute(String parameters) {
        Map<Parameter, Object> parameterMap = parse(parameters);

        return execute(parameterMap);
    }

    private Map<Parameter, Object> parse(String parameters) {
        Map<Parameter, Object> parameterMap = new HashMap<>();

        String[] arr = StringUtils.split(parameters, " ");

        int index = 0;
        while (index < arr.length) {

            if (arr[index].startsWith("-")) {
                String temp = arr[index].replace("-", "");

                if (temp.length() > 1) {
                    for (int i = 0; i < temp.length(); i++) {
                        parameterMap.put(getParameter(String.valueOf(temp.charAt(i))), null);
                    }
                } else if (temp.length() == 1) {
                    if (index + 1 < arr.length && !arr[index + 1].startsWith("-")) {
                        parameterMap.put(getParameter(temp), arr[++index]);
                    } else {
                        parameterMap.put(getParameter(temp), null);
                    }
                } else {
                    throw new CommonRuntimeException("Error format of parameter");
                }
            } else {
                parameterMap.put(getDefaultParameter(), arr[index]);
            }

            ++index;
        }

        return parameterMap;
    }

    protected abstract Parameter getParameter(String value);

    protected Parameter getDefaultParameter() {
        throw new CommonRuntimeException("Error format of parameter");
    }

    protected abstract Message execute(Map<Parameter, Object> parameterMap);


}
