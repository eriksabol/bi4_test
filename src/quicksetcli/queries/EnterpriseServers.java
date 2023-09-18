package quicksetcli.queries;

import com.crystaldecisions.sdk.plugin.desktop.server.IServer;

import java.util.Map;

public interface EnterpriseServers {
    Map<String, IServer> getServersMap();
}
