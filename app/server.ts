import app from "./app";
import {AppConfig} from "./config"
const PORT = AppConfig.PORT;
app.listen(PORT, () => {
    console.log('Express server listening on port ' + PORT);
})