
const env = process.env
export abstract class AppConfig {    
    public static PORT = env.PORT || 3000    
}
