package org.sunbird.util;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.exception.BaseException;
import org.sunbird.common.util.JsonKey;
import org.sunbird.utils.CassandraConnectionManager;
import org.sunbird.utils.CassandraConnectionMngrFactory;

public class DBUtil {
    public static final Map<String, DbInfo> dbInfoMap = new HashMap<>();

    private static void initializeDBProperty() {
        // setting db info (keyspace , table) into static map
        // this map will be used during cassandra data base interaction.
        // this map will have each DB name and it's corresponding keyspace and table
        // name.
        //dbInfoMap.put(JsonKey.USER_DB, getDbInfoObject(KEY_SPACE_NAME, "sunbird_notification"));
    }

    /**
     * This method will check the cassandra data base connection. first it will try to established the
     * data base connection from provided environment variable , if environment variable values are
     * not set then connection will be established from property file.
     */
    public static void checkCassandraDbConnections() throws BaseException {
        CassandraConnectionManager cassandraConnectionManager =
                CassandraConnectionMngrFactory.getInstance();
        String nodes = System.getenv(JsonKey.SUNBIRD_CASSANDRA_IP);
        String[] hosts = null;
        if (StringUtils.isNotBlank(nodes)) {
            hosts = nodes.split(",");
        } else {
            hosts = new String[] {"localhost"};
        }
        cassandraConnectionManager.createConnection(hosts);
    }

    private static DbInfo getDbInfoObject(String keySpace, String table) {

        DbInfo dbInfo = new DbInfo();

        dbInfo.setKeySpace(keySpace);
        dbInfo.setTableName(table);

        return dbInfo;
    }

    /** class to hold cassandra db info. */
    public static class DbInfo {
        private String keySpace;
        private String tableName;
        private String userName;
        private String password;
        private String ip;
        private String port;

        /**
         * @param keySpace
         * @param tableName
         * @param userName
         * @param password
         */
        DbInfo(
                String keySpace,
                String tableName,
                String userName,
                String password,
                String ip,
                String port) {
            this.keySpace = keySpace;
            this.tableName = tableName;
            this.userName = userName;
            this.password = password;
            this.ip = ip;
            this.port = port;
        }

        /** No-arg constructor */
        DbInfo() {}

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DbInfo) {
                DbInfo ob = (DbInfo) obj;
                if (this.ip.equals(ob.getIp())
                        && this.port.equals(ob.getPort())
                        && this.keySpace.equals(ob.getKeySpace())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        public String getKeySpace() {
            return keySpace;
        }

        public void setKeySpace(String keySpace) {
            this.keySpace = keySpace;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getIp() {
            return ip;
        }

        public String getPort() {
            return port;
        }
    }
}
