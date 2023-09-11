package quicksetcli.others;

public class Constants {
    public static final String ALL = "ALL";
    public static final String ONLY_AUTO = "ONLY_AUTO";
    public static final String DASH = "-";
    public static final Integer CMS_PORT = 6400;
    public static final Integer SIA_PORT = 6410;
    public static final Integer WACS_PORT = 6405;
    public static final Integer CMS_REQUEST_PORT = 6401;
    public static final String VIEW_MENU_OPTION = "View";
    public static final String MODIFY_SINGLE_SERVER_MENU_OPTION = "Modify single server";
    public static final String MODIFY_MULTIPLE_SERVERS_MENU_OPTION = "Mass-modify multiple servers";
    public static final String BACK_MENU_OPTION = "← Back";
    public static final String SERVER_QUERY = "SELECT * FROM CI_SYSTEMOBJECTS WHERE SI_KIND='Server' order by SI_NAME ASC";
}
