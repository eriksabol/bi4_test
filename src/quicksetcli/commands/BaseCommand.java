package quicksetcli.commands;

import com.businessobjects.sdk.plugin.desktop.common.IExecProps;
import com.businessobjects.sdk.plugin.desktop.common.IMetric;
import com.businessobjects.sdk.plugin.desktop.common.IMetrics;
import com.crystaldecisions.sdk.exception.SDKException;
import com.crystaldecisions.sdk.occa.infostore.IInfoObject;
import com.crystaldecisions.sdk.occa.infostore.IInfoObjects;
import com.crystaldecisions.sdk.plugin.desktop.server.IServer;
import com.crystaldecisions.sdk.plugin.desktop.server.IServerMetrics;
import quicksetcli.others.Constants;
import quicksetcli.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static quicksetcli.others.Helper.printEmptyLines;

public abstract class BaseCommand implements Command {

    public abstract void execute();

    public IExecProps getExecProps(IServer server) {
        try {
            return server.getContainer().getExecProps();
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }
    }

    public String getActualPort(IServer server) {

        IExecProps serverExecProps = getExecProps(server);
        Pattern pattern = Pattern.compile("-requestport\\s[0-9]{1,5}");
        Matcher matcher = pattern.matcher(serverExecProps.getArgs());

        if (matcher.find()) {

            String requestPortString = matcher.group();
            String[] portNumberArray = requestPortString.split("-requestport\\s");

            return String.join("", portNumberArray);

        }

        return Constants.DASH;
    }

    public String getRunningPort(IServer server) {

        try {
            if (server.isAlive() && server.getState() != null) {

                IServerMetrics serverMetrics = server.getMetrics();
                IMetrics metrics = serverMetrics.getMetrics("ISGeneralAdmin");

                for (Object m : metrics) {
                    IMetric metric = (IMetric) m;

                    if (metric.getName().equals("ISPROP_GEN_HOST_PORT")) return metric.getValue().toString();

                }

            }
        } catch (SDKException e) {
            throw new RuntimeException(e);
        }

        return Constants.DASH;
    }

    public List<Integer> getRunningPorts(Map<String, IServer> serverMap) {
        return serverMap.values().stream()
                .map(this::getRunningPort)
                .filter(port -> port.matches("\\d+"))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    public List<Integer> getActualPorts(Map<String, IServer> serverMap) {
        return serverMap.values().stream()
                .map(this::getActualPort)
                .filter(port -> port.matches("\\d+"))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    public String getModifiedArguments(String pattern, String replacementString, String serverArgs) {

        String modifiedServerExecProps = serverArgs.replaceAll(pattern, replacementString);

        Pattern myPattern = Pattern.compile(pattern);
        Matcher matcher = myPattern.matcher(modifiedServerExecProps);
        boolean matching = matcher.find();

        if (modifiedServerExecProps.equals(serverArgs) && !matching) {
            modifiedServerExecProps += " " + replacementString;
        }

        return modifiedServerExecProps;
    }

    public Map<String, IServer> initializeServerMap(Service service, String serverQuery) {

        Map<String, IServer> serverMap = new LinkedHashMap<>();

        IInfoObjects myInfoObjects;

        System.out.print("Initializing server map...");

        try {
            myInfoObjects = service.getMyInfoStore().query(serverQuery);

            for (Object e : myInfoObjects) {

                IInfoObject myInfoObject = (IInfoObject) e;
                IServer server = (IServer) myInfoObject;
                serverMap.put(server.getCUID(), server);

            }

        } catch (SDKException e) {
            throw new RuntimeException(e);
        }
        System.out.print("done.\n");
        printEmptyLines(1);

        return serverMap;

    }

    public String getConfiguredXmx(IServer server) {
        IExecProps serverExecProps = getExecProps(server);
        Pattern patternXmx = Pattern.compile("Xmx[0-9]{1,5}[m,g]{1}");
        Matcher matcherXmx = patternXmx.matcher(serverExecProps.getArgs());

        return matcherXmx.find() ? matcherXmx.group() : Constants.DASH;
    }

    public String getActiveXmx(IServer server) {
        Pattern patternXmx = Pattern.compile("Xmx[0-9]{1,5}[m,g]{1}");
        String currentCommandLine = server.getCurrentCommandLine();

        if(Objects.isNull(currentCommandLine)) {
            return Constants.DASH;
        }
        else {
            Matcher matcherSetXmx = patternXmx.matcher(currentCommandLine);
            return matcherSetXmx.find() ? matcherSetXmx.group() : Constants.DASH;
        }
    }

    public String getPortSetMethod(IServer server) {

        IExecProps serverExecProps = getExecProps(server);
        String actualServerExecProps = serverExecProps.getArgs();

        if (actualServerExecProps.contains("-requestport")) return "Manual";

        return "Auto";

    }
}
