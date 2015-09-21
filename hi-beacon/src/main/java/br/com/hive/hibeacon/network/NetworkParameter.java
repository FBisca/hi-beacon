package br.com.hive.hibeacon.network;

/**
 * Created by FBisca on 01/06/2015.
 */
public class NetworkParameter {

    private String name;
    private Object value;
    private ParameterType type;

    NetworkParameter(String name, Object value, ParameterType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    Object getValue() {
        return value;
    }

    String getName() {
        return name;
    }

    ParameterType getType() {
        return type;
    }


    public enum ParameterType {
        BINARY(true), NORMAL(false), IMAGE_PNG(true), IMAGE_JPG(true);

        private boolean multipart = false;

        ParameterType(boolean multipart) {
            this.multipart = multipart;
        }

        public boolean isMultipart() {
            return multipart;
        }

        public String getMimeType() {
            switch (this) {
                case NORMAL:
                    return "text/plain";
                case BINARY:
                    return "application/octet-stream";
                case IMAGE_PNG:
                    return "image/png";
                case IMAGE_JPG:
                    return "image/jpeg";
            }
            return "";
        }
    }
}
