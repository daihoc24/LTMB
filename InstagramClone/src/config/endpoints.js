// import { REACT_APP_API_BASE_URL } from '@env';
// const SERVER_ADDRESS = `192.168.1.21`;
const SERVER_ADDRESS = `10.225.63.225`;
const REACT_APP_API_BASE_URL = `http://${SERVER_ADDRESS}:8080/chat-application/v1`;

const API_BASE_URL = REACT_APP_API_BASE_URL;
// ChatRealTime service chạy port 8082 (server.port=8082)
const API_WEBSOCKET = `http://${SERVER_ADDRESS}:8082`;

const ENDPOINTS = {
    AUTH: {
        GET_TOKEN: `${API_BASE_URL}/auth/token`,
        INTROSPECT: `${API_BASE_URL}/auth/introspect`,
    },
};

export default ENDPOINTS;
