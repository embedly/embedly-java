package com.embedly.api;

import static com.embedly.api.Utils.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * Api is used to access the Embedly API and Pro endpoints.
 */
public class Api {
    private String key;
    private String host;
    private String userAgent;

    private DefaultHttpClient _httpClient;
    private ResponseHandler<String> _responseHandler;

	public Api(String userAgent) {
        this(userAgent, null, null);
    }

    public Api(String userAgent, String key) {
        this(userAgent, key, null);
    }

    /**
     * The Api object is used to access both Embedly API
     * and Embedly Pro.  When a key is used, the Pro endpoints
     * oembed, preview and objectify are available.  When no
     * key is used, the API endpoints services and oembed are
     * available.
     *
     * A userAgent containing, at the very least, a contact
     * email address for the client applications operator.  This
     * is used so that Embedly can contact the client in the case
     * of any issues or improper use.  Your client may be blocked
     * by embedly if a contact address is not provided.
     *
     * ex. "Mozilla/5.0 (compatible; myapp/1.0; +my@email.com)
     *
     * @param userAgent A userAgent string containing contact information
     *
     * @param key       Your Embedly PRO key
     * 
     * @param host      An alternative hostname, used for debugging.  
     *                  ex. "http://localhost"
     */
    public Api(String userAgent, String key, String host) {
        this.userAgent = userAgent;
        this.key = key;
        this.host = host;

        if (this.userAgent == null) {
            throw new RuntimeException(
                    "You must specify a userAgent when constructing an " +
                    "Api object");
        }

        if (this.key == null && this.host == null) {
            this.host = "http://api.embed.ly";
        } else if (this.host == null) {
            this.host = "http://pro.embed.ly";
        }

        // prime these
        getHttpClient();
        getResponseHandler();
    }

    /**
     * Call the oembed endpoint.  Valid for both API and Pro instances.
     * 
     * <a target="_top" href="https://pro.embed.ly/docs/oembed">Embedly Oembed Docs</a>
     *
     * @param params A map of name value pairs.  The value should be either
     *               a String, or a String[].  At this time, String[] is only
     *               valid for the "urls" parameter.
     *
     * @return JSONArray of JSONObjects.  
     */
    public JSONArray oembed(Map<String, Object> params) {
        return this.apicall("1", "oembed", params);
    }

    /**
     * Call the objectify endpoint.  Valid for Pro instances.
     *
     * <a target="_top" href="https://pro.embed.ly/docs/objectify">Embedly Objectify Docs</a>
     *
     * @param params A map of name value pairs.  The value should be either
     *               a String, or a String[].  At this time, String[] is only
     *               valid for the "urls" parameter.
     *
     * @return JSONArray of JSONObjects.  
     */
    public JSONArray objectify(Map<String, Object> params) {
        return this.apicall("2", "objectify", params);
    }

    /**
     * Call the preview endpoint.  Valid for Pro instances.
     *
     * <a target="_top" href="https://pro.embed.ly/docs/preview">Embedly Preview Docs</a>
     *          
     * @param params A map of name value pairs.  The value should be either
     *               a String, or a String[].  At this time, String[] is only
     *               valid for the "urls" parameter.
     *
     * @return JSONArray of JSONObjects.  
     */
    public JSONArray preview(Map<String, Object> params) {
        return this.apicall("1", "preview", params);
    }

    /**
     * Returns a JSON array from the API services endpoint.  This method
     * is only applicable to API hosts.
     * 
     * <a target="_top" href="http://api.embed.ly/docs/service">Embedly Services Docs</a>
     *
     * @return JSONArray of services 
     */
    public JSONArray services() {
        JSONArray resp = null;
        try {
            // fail safe response
            resp = new JSONArray("[]");

            if (key != null) {
            	getLog().error("Pro doesn't support services");
                throw new RuntimeException("Pro doesn't support services");
            }

            String call = this.host+"/1/services/javascript";
            resp = new JSONArray(simpleHTTP(getHttpClient(), getResponseHandler(), 
            		call, getHeaders()));
        } catch (JSONException e) {
        	getLog().error("Failed to parse JSON in response", e);
            throw new RuntimeException("Failed to parse JSON in response", e);
        } catch (IOException e) {
        	getLog().error("HTTP call failed", e);
            throw new RuntimeException("HTTP call failed", e);
        }
        return resp;
    }
    
    private Map<String, String> getHeaders() {
    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put("User-Agent", userAgent);
    	headers.put("X-Embedly-Client", "embedly-java-0.1.1");
    	return headers;
    }

    /**
     * Returns a pattern object to match against URLs.  This method is only
     * applicable to API hosts, since Pro accepts any URL.
     *
     * @return Pattern for URLs valid on API endpoints
     */
    public Pattern servicesPattern() {
        try {
            JSONArray services = services();
            ArrayList<String> regexList = new ArrayList<String>();
            for (int i = 0; i < services.length(); ++i) {
                JSONObject obj = services.getJSONObject(i);
                JSONArray regexes = obj.getJSONArray("regex");
                for (int j = 0; j < regexes.length(); ++j) {
                    regexList.add(regexes.getString(j));
                }
            }
            Pattern ret = Pattern.compile(stringJoin(regexList, "|"));
            return ret;
        } catch (PatternSyntaxException e) {
        	getLog().error("Unexpected issue with services response", e);
            throw new RuntimeException(
                    "Unexpected issue with services response", e);
        } catch (JSONException e) {
        	getLog().error("Unexpected issue with services response", e);
            throw new RuntimeException(
                    "Unexpected issue with services response", e);
        }
    }

    public String toString() {
        return "com.embedly.api.Api[key="+key+",host="+host+",userAgent="+
            userAgent+"]";
    }

    /**
     * Generic Embedly endpoint call.  This shouldn't be called directly, but
     * is used internally by the oembed, objectify and preview endpoint
     * methods.
     *
     * <a target="_top" href="https://pro.embed.ly/docs/">Embedly Docs</a>
     *
     * @param version Endpoint version
     * 
     * @param action  Endpoint name
     *
     * @param params  A map of name value pairs.  The value should be either
     *                a String, or a String[].  At this time, String[] is only
     *                valid for the "urls" parameter.
     *
     * @return JSONArray of JSONObjects.  
     */
    private JSONArray apicall(String version, String action,
            Map<String, Object> params) {
        ResponseMaker resp = new ResponseMaker();
        try {
            ApiParameters query = new ApiParameters();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof String[]) {
                    query.push(entry.getKey().toString(),
                            (String[])entry.getValue());
                } else {
                    query.push(entry.getKey().toString(),
                            entry.getValue().toString());
                }
            }

            ArrayList<String> urls = query.getParam("urls");
            if (key != null) {
                query.push("key", key);
                urls = resp.prepare(urls, Pattern.compile(".*"));
            } else {
                getLog().debug("checking urls against services");
                urls = resp.prepare(urls, servicesPattern());
            }

            if (urls.size() > 0) {
                String call = this.host+"/"+version+"/"+action+"?"+
                    query.toQuery();
                String json_text = simpleHTTP(getHttpClient(), 
                		getResponseHandler(), call, getHeaders());
                resp.fill(new JSONArray(json_text));
            }

        } catch (JSONException e) {
            getLog().error("Failed to parse JSON in response", e);
            throw new RuntimeException("Failed to parse JSON in response", e);
        } catch (UnsupportedEncodingException e) {
        	getLog().error("Parameters couldn't be encoded with utf-8", e);
            throw new RuntimeException(
                    "Parameters couldn't be encoded with utf-8", e);
        } catch (IOException e) {
        	getLog().error("HTTP call failed", e);
            throw new RuntimeException("HTTP call failed", e);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Returning >> "+resp);
        }
        return resp.getResponse();
    }

    /**
     * Set log.  Try Api.setLog(LogFactory.getLog(Api.class));
     *
     * We do this instead of just setting in statically for android's sake.
     * All package visible classes share the same logger.. sorry.
     */
	public static void setLog(Log log) {
		Utils.setLog(log);
	}
    
    private HttpClient getHttpClient() {
        if (_httpClient == null) {
            _httpClient = new DefaultHttpClient();
        }
        return _httpClient;
    }

    private ResponseHandler<String> getResponseHandler() {
        if (_responseHandler == null) {
            _responseHandler = new BasicResponseHandler();
        }
        return _responseHandler;
    }

    /**
     * Set Timeout on Blocking Socket Operations
     * @param timeout Timeout in milliseconds
     */
    public void setSoTimeout( int timeout ) {
    	DefaultHttpClient client = getHttpClient();
    	HttpParams params = client.getParams();
    	HttpConnectionParams.setSoTimeout( params, timeout );
    	client.setParams( params );    	    	    	
    }	
}
