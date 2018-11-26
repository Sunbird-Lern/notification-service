import * as ExpressCassandra from 'express-cassandra';
import * as path from 'path';
export class CassandraAdapter {
    private static cassandraAdapter:any;
    private constructor(){
        const modelsPath = path.join(__dirname ,'../../../../app/modules/shared/models')
        ExpressCassandra.setDirectory(modelsPath).bind(
            {
                clientOptions: {
                    contactPoints: ['127.0.0.1'],
                    protocolOptions: { port: 9042 },
                    keyspace: 'sunbird_notifications',
                    queryOptions: {consistency: ExpressCassandra.consistencies.one}
                },
                ormOptions: {
                    defaultReplicationStrategy : {
                        class: 'SimpleStrategy',
                        replication_factor: 1
                    },
                    migration: 'safe'
                }
            },
            function(err) {
                if(err) throw err;
        
                // You'll now have a `person` table in cassandra created against the model
                // schema you've defined earlier and you can now access the model instance
                // in `models.instance.Person` object containing supported orm operations.
            }
        );
        return ExpressCassandra;
    }
    public static connect(){
        // always return singleton instance of CassandraAdapter
        if(!CassandraAdapter.cassandraAdapter){
            CassandraAdapter.cassandraAdapter = new CassandraAdapter();
        }
        return CassandraAdapter.cassandraAdapter;
    }

}