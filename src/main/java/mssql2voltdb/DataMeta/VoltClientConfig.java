package mssql2voltdb.DataMeta;

import java.util.List;

/**
 * Created by swqsh on 2017/4/10.
 */
public class VoltClientConfig {

    private String userName="";

    private String passWord="";

    private List<String> servers;

    private boolean topologyChangeAware=false;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }

    public boolean isTopologyChangeAware() {
        return topologyChangeAware;
    }

    public void setTopologyChangeAware(boolean topologyChangeAware) {
        this.topologyChangeAware = topologyChangeAware;
    }

}
