package de.prob.webconsole.servlets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.prob.annotations.Home;
import de.prob.webconsole.GroovyExecution;
import de.prob.webconsole.ResultObject;

/**
 * This servlet takes a line from the web interface and evaluates it using
 * Groovy. The Groovy interpreter does not remember import statements, i.e., the
 * input 'import foo.Bar; x = new Bar' will work, but spliting it into two
 * separate lines won't. We thus collect any import statement and prefix every
 * command with all the imports.
 * 
 * @author bendisposto
 * 
 */
@SuppressWarnings("serial")
@Singleton
public class GroovyShellServlet extends HttpServlet {

	private final GroovyExecution executor;
	private BufferedWriter bw;
	
	@Inject
	public GroovyShellServlet(GroovyExecution executor, @Home final String probhome) {
		this.executor = executor;
		final File scrollback = new File(probhome + "scrollback");
		try {
			bw = new BufferedWriter(new FileWriter(scrollback, true));
		} catch (IOException e) {
			bw = null;
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		PrintWriter out = res.getWriter();
		String input = req.getParameter("input");
		
		if (bw != null) {
			bw.write(input);
			bw.newLine();
			bw.flush();
		}
		
		String result = executor.evaluate(input);

		ResultObject r = new ResultObject(result, executor.isContinued());

		out.println(toJson(r));
		out.close();
	}

	/**
	 * Converts the ResultObject into a JSON representation
	 * 
	 * @param result
	 * @return
	 */
	private String toJson(ResultObject result) {
		Gson g = new Gson();
		String json = g.toJson(result);
		return json;
	}

}