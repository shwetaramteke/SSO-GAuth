package com.saama;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.saama.helper.GetUserInfo;

@WebServlet("/oauth2callback")
public class oauth2callback extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public oauth2callback() {
		super();

	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
		String error = req.getParameter("error");
		if ((null != error) && ("access_denied".equals(error.trim()))) {
			HttpSession sess = req.getSession();
			sess.invalidate();
			resp.sendRedirect(req.getContextPath());
			return;
		}

		AsyncContext ctx = req.startAsync();
		ctx.start(new GetUserInfo(req, resp, ctx));
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}