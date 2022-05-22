package ua.martishyn.app.controller.commands.admin.station;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.martishyn.app.controller.commands.ICommand;
import ua.martishyn.app.controller.filters.HasRole;
import ua.martishyn.app.data.dao.impl.StationDaoImpl;
import ua.martishyn.app.data.dao.interfaces.StationDao;
import ua.martishyn.app.data.entities.enums.Role;
import ua.martishyn.app.data.utils.Constants;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@HasRole(role = Role.ADMIN)
public class StationDeleteCommand implements ICommand {
    private static final Logger log = LogManager.getLogger(StationDeleteCommand.class);

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (deleteStation(request)) {
            log.info("Station deleted successfully");
            response.sendRedirect("stations-page.command");
        } else {
            RequestDispatcher requestDispatcher = request.getRequestDispatcher(Constants.ADMIN_STATIONS);
            requestDispatcher.forward(request, response);
        }
    }

    private boolean deleteStation(HttpServletRequest request) {
        StationDao stationDao = new StationDaoImpl();
        int id = Integer.parseInt(request.getParameter("id"));
        return stationDao.delete(id);
    }
}
