/**
 * University of Illinois/NCSA
 * Open Source License
 * 
 * Copyright (c) 2008, Board of Trustees-University of Illinois.  
 * All rights reserved.
 * 
 * Developed by: 
 * 
 * Automated Learning Group
 * National Center for Supercomputing Applications
 * http://www.seasr.org
 * 
 *  
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal with the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions: 
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimers. 
 * 
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimers in the 
 *    documentation and/or other materials provided with the distribution. 
 * 
 *  * Neither the names of Automated Learning Group, The National Center for
 *    Supercomputing Applications, or University of Illinois, nor the names of
 *    its contributors may be used to endorse or promote products derived from
 *    this Software without specific prior written permission. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * WITH THE SOFTWARE.
 */ 
package org.seasr.meandre.components.weka.viz.feature;

import org.meandre.core.ComponentContext;
import org.meandre.core.ComponentContextException;
import org.meandre.core.ComponentExecutionException;
import org.meandre.webui.WebUIException;
import org.meandre.webui.WebUIFragmentCallback;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.util.concurrent.Semaphore;
import java.util.Hashtable;
import java.util.Enumeration;
/**
 * StaticWebPage holds a set of text (typically html or css) docs and/or
 * a set of images and serves them up through the WebUIFragmentCallback
 * interface (basically a servlet container). StaticWebPage also takes
 * care of generating urls so the html docs can link to each other
 * and reference the images. It's static in the sense that once it
 * has been started up (see runSession()) the contents shouldn't be
 * modified.
 * 
 *
 * It's intended usage is for a Meandre Component to construct a
 * StaticWebPage at runtime (it requires an active ComponentContext to
 * initialize) and then delegate all requests for content to it. 
 * 
 * 
 */
public class StaticWebPage implements WebUIFragmentCallback {

 	 /**
     * The blocking semaphore. Used to leave the local webserver running
	 * until the "Dismiss" link is clicked.
     */
    private Semaphore _sem = null;

    /**
     * The instance ID. Unique to the flow run (or maybe the component?).
	 * The Urls that get generated to access content are keyed off this.
     */
    private String _runId = null;

	/** a hook to the component context we're running in*/
	private ComponentContext _meandreContext = null;

	/**
	 * the key into _textDocs for the page that will be returned by getMainPage.
	 */
	private static String MAIN_PAGE_KEY = "index.html";

	/** the http query string parameter name for text documents.*/
	private static String TEXT_DOC_PARAM = "doc";

	/**the http query string parameter for images*/
	private static String IMAGE_PARAM = "img";

	/**the http query string parameter to stop the local webserver and 
	 * finish execution.  */
	private static String DISMISS_PARAM = "done";

	/**
	 * A lookup table from a string key (used as http query parameter values)
	 * to a text document (comparable what would be in a .html or .css file).
	 */
	private Hashtable<String,String> _textDocs = null;

	/**
	 * A lookup table from a string key (used as http query parameter values)
	 * to an image (comparable to what would be in a .png file).
	 */
	private Hashtable<String,byte[]> _images = null;

	/** 
	 * Initializes a space for static files that is dependent on a Meandre
	 * runtime.
	 */
	public StaticWebPage(ComponentContext cc){
		_sem = new Semaphore(1, true);
		_runId = cc.getExecutionInstanceID();
		_meandreContext = cc;
		_textDocs = new Hashtable<String,String>();
		_images = new Hashtable<String,byte[]>();
	}

	/////////////////
	//Page Setup
	/////////////////

	/**
	 * returns a relative url that can be used to link back to the main
	 * page from a supporting page or file. the returned string
	 * is equivalent to resolveURL("index.html")
	 */
	public String setMainPage(String htmlDoc){
		String backLink = addTextFile(htmlDoc, MAIN_PAGE_KEY);
		return backLink;
	}	

	/**
	 * returns an html document as a String.
	 */
	public String getMainPage(){
		return _textDocs.get(MAIN_PAGE_KEY);
	}

	/**
	 * returns a snippet of html that contains a link that, when
	 * clicked, will stop the webpage session and close the
	 * browser window the page is being displayed in. The creator
	 * of the webpage is responsible for embedding this snippet in
	 * one or all of the text docs (normally at least in mainPage).
	 */
	public String getDismissButtonSnippet(){
		
		String url = "/" + _runId + "?done=true";

		StringBuffer sb = new StringBuffer();
		sb.append("<br><div style=\"background-color:#DBD1D1;width:100%\">");
		sb.append("<center>");
		sb.append("<a href=\"" + url + "\">Dismiss</a>");
		sb.append("</center></div>");
		return sb.toString();
	}

	/**
	 * returns the relative url for the page after writing it to a file.
	 * returned url is equivalent to <code>resolveURL(fileNameBase)</code>
	 */
	public String addTextFile(String fileContents, String fileNameBase){
		_textDocs.put(fileNameBase, fileContents);
		String linkPath = resolveURL(TEXT_DOC_PARAM, fileNameBase);
		return linkPath;
	}

	/**constructs the url to use to access an image file with the given name.
	 * this is equivalent to the url returned by addTextFile(str, fileBasename).
	 * use this to gather urls for hyperlinks between html docs that will
	 * be added using addTextFile() once constructed.
	 */
	public String resolveDocURL(String fileNameBase){
		return resolveURL(TEXT_DOC_PARAM, fileNameBase);
	}

	/** writes a BufferedImage to a png. the input filename should end
	 * in ".png". 
	 * returned url is equivalent to <code>resolveURL(fileNameBase)</code>
	 */
	public String addImageFile(BufferedImage img, String fileNameBase){
		try{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			ImageIO.write(img, "png", buffer);
			byte[] imgBytes = buffer.toByteArray();
			_images.put(fileNameBase, imgBytes);

		}catch(Exception e){
			e.printStackTrace();
		}
		String linkPath = resolveURL(IMAGE_PARAM, fileNameBase);
		return linkPath;
	}


	/**constructs the url to use to access an image file with the given name.
	 * this is equivalent to the url returned by addImageFile(img, fileBasename)
	 */
	public String resolveImageURL(String fileNameBase){
		return resolveURL(IMAGE_PARAM, fileNameBase);
	}

	/**
	 * returns a url to point to an added file. depends on a fileSetName
	 * which is something like "doc" or "img". do not use directly.
	 * */
	private String resolveURL(String fileSetName, String fileBasename){
		String url = "/" + _runId + "?" + fileSetName + "=" + fileBasename;
		return url;
	}


	///////////////////////////////////
	//WebUIFragmentCallback Interface and Session Management
	//(These methods apply after the page is built and now needs
	//to be viewed by users)
	//////////////////////////////////
	
	/**
	 * start up the webUIFragment and doesn't return until the user has 
	 * dismissed it through an http request.
	 */
	public void runSession(){
		try{
			_sem.acquire();//get the only semaphore
			_meandreContext.startWebUIFragment(this);

			//wait for a semaphore (won't unlock until "done" is clicked)
			_sem.acquire();
	
			_meandreContext.stopWebUIFragment(this);
		}catch(Exception e){
			e.printStackTrace();
			//TODO: no good way to fail
		}
	}

    /**
     * This method gets called when a call with parameters is done to a 
	 * given component's webUI fragment
     *
     * @param request  The request object
     * @param response The response object
     * @throws WebUIException A problem occurred during the call back
     */
    public void handle(HttpServletRequest request, HttpServletResponse response)
            throws WebUIException {

		Enumeration<String> httpParamNames = 
				(Enumeration<String>)request.getParameterNames();

		if(!httpParamNames.hasMoreElements()){
			//this error message will be shown to the user in the browser
			try{
				response.setContentType("text/html");
				response.getWriter().println(
						"Error: StaticWebPage: no query params");
				response.getWriter().flush();
			}catch(IOException e){
				throw new WebUIException(e);
			}
			//TODO: log an error message w/ Meandre
			return;
		}
		String paramName = httpParamNames.nextElement();
		String paramValue = request.getParameter(paramName);
		try{
			if(paramName.equals(DISMISS_PARAM)){
					_sem.release();
			}else if(paramName.equals(IMAGE_PARAM)){
				response.reset();
				//response.resetBuffer();
				//response.setContentType("image/png");
				byte[] imgBytes = _images.get(paramValue);
				//response.setContentType("image/png");
				ServletOutputStream ops = response.getOutputStream();
				response.setContentLength(imgBytes.length);
				response.setContentType("image/png");
				response.setStatus(HttpServletResponse.SC_OK);
				ops.write(imgBytes);
				response.flushBuffer();
				//ops.flush();

				//java.io.File fl = new java.io.File("/dat/tmp/k" + paramValue);
				//java.io.FileOutputStream writer = new java.io.FileOutputStream(fl);
				//writer.write(imgBytes);
				//ops.close();
			}else if(paramName.equals(TEXT_DOC_PARAM)){
				response.setContentType("text/html");
				PrintWriter ops = response.getWriter();
				String docStr = _textDocs.get(paramValue);
				ops.print(docStr);
				ops.close();
			}else{
				response.setContentType("text/html");
				ServletOutputStream ops = response.getOutputStream();
				ops.println("Error: StaticWebPage: invalid param: " + paramName);
				ops.close();
			}
		}catch(IOException e){
			throw new WebUIException(e);
		}
		return;
    }
    /**
     * This method gets call when a request with no parameters is made to a
     * component WebUI fragment.
     *
     * @param response The response object
     * @throws WebUIException Some problem encountered during execution
     *                        and something went wrong
     */
    public void emptyRequest(HttpServletResponse response)
            throws WebUIException {
        try {
			response.setContentType("text/html");
            response.getWriter().println(this.getMainPage());
        }
        catch (IOException e) {
            throw new WebUIException(e);
        }
		return;
    }
}
