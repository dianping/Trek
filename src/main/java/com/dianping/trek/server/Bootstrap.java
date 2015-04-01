package com.dianping.trek.server;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class Bootstrap extends GenericServlet{
    
    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        TrekServer server = new TrekServer();
        try {
            server.initParam();
        } catch (Exception e) {
            throw new ServletException("servlet init fail", e);
        }
        server.setName("Trek-Server-Main");
        server.start();
    }

    @Override
    public void service(ServletRequest req, ServletResponse res)
            throws ServletException, IOException {
    }
}
