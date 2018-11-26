
const env = process.env
export abstract class AppConfig {    
    public static PORT = env.sunbird_notification_service_port || 3000    
    public static KAFKA_TOPICS = env.sunbird_notification_service_kafka_topics || 'normal,immediate'
    public static SMTP_CONFIG = {
        USERNAME: env.sunbird_notification_service_smtp_username,
        PASSWORD: env.sunbird_notification_service_smtp_password
    }
}
