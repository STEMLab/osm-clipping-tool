package io.github.stemlab.session;

import io.github.stemlab.entity.TableWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Created by Azamat on 7/31/2017.
 */
@Component
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Database {

    private String driver = "org.postgresql.Driver";
    private String port;
    private String host;
    private String name;
    private String user;
    private String password;
    private TableWrapper tableWrapper;
    private boolean isDBDefined;
    private boolean isRelationDefined;

    public boolean isDBDefined() {
        return isDBDefined;
    }

    public void setDBDefined(boolean DBDefined) {
        isDBDefined = DBDefined;
    }

    public boolean isRelationDefined() {
        return isRelationDefined;
    }

    public void setRelationDefined(boolean relationDefined) {
        isRelationDefined = relationDefined;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public TableWrapper getTableWrapper() {
        return tableWrapper;
    }

    public void setTableWrapper(TableWrapper tableWrapper) {
        this.tableWrapper = tableWrapper;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDriver() {
        return driver;
    }

    public void setHost(String url) {
        this.host = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnection() {
        return "jdbc:postgresql://" + this.host + ":" + this.port + "/" + this.name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
