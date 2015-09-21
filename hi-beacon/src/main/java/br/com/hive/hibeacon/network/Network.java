package br.com.hive.hibeacon.network;

import android.os.AsyncTask;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Network {

	/**
	 *
	 * @param url
	 * @return
	 */
    public static Builder create(String url) {
        return new Builder(url);
    }

	private static HttpURLConnection prepareRequest(Builder builder)
			throws IOException, JSONException, IllegalStateException, NullPointerException {
		if (builder.method.equals(Method.GET)) {
			builder.url = builder.url + preparaGet(builder);
		}

		URL url = new URL(builder.url);
		HttpURLConnection.setFollowRedirects(builder.followRedirects);

		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        urlConnection.setConnectTimeout(builder.connectionTimeout);
        urlConnection.setReadTimeout(builder.readTimeout);

		urlConnection.setRequestProperty("Accept-Charset", builder.charset);

		if (builder.method.equals(Method.POST)
				|| builder.isRawRequest()) {
			prepareFormEncoded(urlConnection, builder);
		}

		return urlConnection;
	}

	private static String request(Builder builder)
			throws IOException, JSONException, IllegalStateException, NullPointerException {

		URLConnection urlConnection = prepareRequest(builder);
		urlConnection.setConnectTimeout(20000);

		InputStream instream = urlConnection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				instream, builder.charset));
		StringBuilder response = new StringBuilder();
		String line = reader.readLine();

		while (line != null) {
			response.append(line);
			line = reader.readLine();
		}
		instream.close();
		reader.close();
		return response.toString();
	}

	private static String preparaGet(Builder builder) throws UnsupportedEncodingException {
		String query = "";

		if (builder.params != null) {
			query += "?";
			Iterator<String> iterator = builder.params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				NetworkParameter parameter = builder.params.get(key);
				if (parameter.getType().equals(NetworkParameter.ParameterType.BINARY)) {
					throw new IllegalArgumentException("Parameter of the type \"BINARY\" cannot be set with the HTTP Method \"GET\"");
				}

				String value = key + "=" + URLEncoder.encode(String.valueOf(parameter.getValue()),
						builder.charset);

				if (iterator.hasNext()) {
					value += "&";
				}

				query += value;
			}
		}

		return query;
	}

	private static void prepareFormEncoded(URLConnection urlConnection, Builder builder) throws IOException {
		urlConnection.setDoOutput(true);

        if (builder.isRawRequest()) {
            OutputStream output = urlConnection.getOutputStream();
            output.write(builder.mRaw.getBytes(builder.charset));
        } else if (builder.hasMultipart()) {
			prepareMultipart(urlConnection, builder);
		} else {
			String query = "";
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + builder.charset);
			Iterator<String> iterator = builder.params.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				NetworkParameter parameter = builder.params.get(key);

				String value = key + "=" + URLEncoder.encode(String.valueOf(parameter.getValue()),
						builder.charset);

				if (iterator.hasNext()) {
					value += "&";
				}

				query += value;
			}

			OutputStream output = urlConnection.getOutputStream();
			output.write(query.getBytes(builder.charset));
		}

	}

	private static void prepareMultipart(URLConnection urlConnection, Builder builder) throws IOException {
		String CRLF = "\r\n";
		String boundary = Long.toHexString(System.currentTimeMillis());

        urlConnection.setUseCaches(false);
		urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        urlConnection.setRequestProperty("Connection", "Keep-Alive");
        urlConnection.setRequestProperty("Cache-Control", "no-cache");

		OutputStream output = urlConnection.getOutputStream();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, builder.charset), true);

		for (String key : builder.params.keySet()) {

			NetworkParameter parameter = builder.params.get(key);
			if (!parameter.getType().isMultipart()) {
				writer.append("--").append(boundary).append(CRLF);
				writer.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(CRLF);
				writer.append("Content-Type: text/plain; charset=").append(builder.charset).append(CRLF);
				writer.append(CRLF).append(parameter.getValue().toString()).append(CRLF).flush();

			} else {
                InputStream stream = (InputStream) parameter.getValue();
                String contentType = URLConnection.guessContentTypeFromStream(stream);

                writer.append("--").append(boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"").append(key).append("\"; filename=\"").append(key).append("\"").append(CRLF);

                if (contentType != null) {
                    writer.append("Content-Type: ").append(contentType).append(CRLF);
                } else {
                    writer.append("Content-Type: " + parameter.getType().getMimeType()).append(CRLF);
                }

                writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                writer.append(CRLF).flush();
                copy(builder, stream, output);
                output.flush();
                writer.append(CRLF).flush();
            }
		}

		writer.append("--").append(boundary).append("--").append(CRLF).flush();
	}

    private static void copy(Builder builder, InputStream copy, OutputStream to) throws IOException {
        int total = copy.available();
        int sent = 0;
        byte[] buf = new byte[1024];
        int len;
        while ((len = copy.read(buf)) > 0) {
            to.write(buf, 0, len);
            if (builder.mListener != null && total > 0) {
                sent += len;
                if (total > 0) {
                    float progress = (sent * 100) / total;
                    builder.mListener.progress(progress, sent, total - sent, total);
                }
            }
        }

    }

	public static class Builder {

		private Map<String, NetworkParameter> params;
		private int connectionTimeout = 15000;
		private int readTimeout = 120000;
		private String url;
		private Method method;
		private String charset;
        private Listener mListener;
        private String mRaw;
		private boolean followRedirects = true;

		private Builder(String url) {
			this.params = new HashMap<>();
			this.method = Method.GET;
			this.url = url.replaceAll("\\s", "_");
			this.charset = "UTF-8";
		}


		public Builder setMethod(Method method) {
			this.method = method;
			return this;
		}

		public Builder setCharset(String charset) {
			this.charset = charset;
			return this;
		}

        public Builder setListener(Listener listener) {
            this.mListener = listener;
            return this;
        }

        public Builder setRawRequest(String raw) {
            this.mRaw = raw;
            return this;
        }

		public Builder setFollowRedirects(boolean followRedirects) {
			this.followRedirects = followRedirects;
			return this;
		}

		public Builder setConnectionTimeout(int connectionTimeout) {
			this.connectionTimeout = connectionTimeout;
			return this;
		}

		public Builder setReadTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
			return this;
		}

		public Builder addParameter(String name, Object value, NetworkParameter.ParameterType type) {
			if (type.isMultipart()) {

                if (value instanceof File) {
                    File file = (File) value;

                    try {
                        value = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        throw new IllegalArgumentException(e.getMessage());
                    }

                } else if (!(value instanceof InputStream)) {
                    throw new IllegalArgumentException("Parameter of the type \"Multipart\" must be an InputStream or a File");
                }

			}

			params.put(name, new NetworkParameter(name, value, type));
			return this;
		}

		public Builder addParameter(String name, Object value) {
			if (value instanceof File
					|| value instanceof InputStream) {
				return addParameter(name, value, NetworkParameter.ParameterType.BINARY);
			} else {
				this.addParameter(name, value, NetworkParameter.ParameterType.NORMAL);
				return this;
			}
		}

		public String request() {
			try {
				String response = Network.request(this);
				if (mListener != null) {
					mListener.onComplete(response);
				}
				return response;
			} catch (Exception e) {
				if (mListener != null) {
                    mListener.onError(e.getMessage());
                }
			}
			return null;
		}

		public HttpURLConnection openConnection() {
			try {
				return Network.prepareRequest(this);
			} catch (Exception e) {
				return null;
			}
		}

        public void requestAsync() {
            new AsyncTask<Builder, Void, String>() {

                private Exception e;

                @Override
                protected String doInBackground(Builder... builders) {
                    try {
                        return Network.request(builders[0]);
                    } catch (Exception e) {
                        this.e = e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (e != null && mListener != null) {
                        mListener.onError(e.getMessage());
                    } else if (mListener != null) {
                        mListener.onComplete(s);
                    }
                }

            }.execute(this);
        }

		public boolean hasMultipart() {
			boolean hasMultipart = false;
			for (String key : params.keySet()) {

				NetworkParameter parameter = params.get(key);
				if (parameter.getType().equals(NetworkParameter.ParameterType.BINARY)) {
					hasMultipart = true;
				}
			}

			return hasMultipart;
		}

        public boolean isRawRequest() {
            return mRaw != null;
        }
	}

    public interface Listener {
        void onError(String message);
        void onComplete(String response);
        void progress(float progress, int bytesSent, int bytesLeft, int totalBytes);
    }

	public enum Method {
		GET, POST
	}

}
