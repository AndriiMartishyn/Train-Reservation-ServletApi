package ua.martishyn.app.data.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.martishyn.app.data.dao.interfaces.TicketDao;
import ua.martishyn.app.data.dao.interfaces.TrainAndModelDao;
import ua.martishyn.app.data.entities.Ticket;
import ua.martishyn.app.data.entities.Train;
import ua.martishyn.app.data.utils.DataBasePoolManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketDaoImpl implements TicketDao {
    private static final Logger log = LogManager.getLogger(TicketDaoImpl.class);
    private static final String CREATE_TICKET = "INSERT INTO tickets (id, user_id, train_id, first_name, last_name,  " +
            "departure_station, departure_time, arrival_station, arrival_time, place, wagon, comfort_class, price, isPaid) " +
            "VALUES (?, ?, ?, ?, ?, ? , ?, ?, ? , ?, ? , ?, ?, ?);";
    private static final String GET_TICKETS_BY_USER_ID = "SELECT * FROM tickets WHERE user_id = ?;";
    private static final String GET_TICKETS_COUNT_BY_PLACE_AND_WAGON = "SELECT COUNT(*) as quantity FROM tickets WHERE wagon = ? AND place = ?;";
    private static final String UPDATE_TICKET_PAID_STATUS = "UPDATE tickets SET isPaid = ? WHERE id = ?;";

    @Override
    public boolean createTicket(Ticket ticket) {
        try (Connection connection = DataBasePoolManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CREATE_TICKET)) {
            createTicketStatement(preparedStatement, ticket);
            if (preparedStatement.executeUpdate() > 0) {
                return true;
            }
        } catch (SQLException e) {
            log.error("Problems when creating ticket {}", e.toString());
            return false;
        }
        return false;
    }

    @Override
    public boolean getByPlaceAndWagon(int wagon, int place) {
        int quantity = 0;
        try (Connection connection = DataBasePoolManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_TICKETS_COUNT_BY_PLACE_AND_WAGON)) {
            preparedStatement.setInt(1, wagon);
            preparedStatement.setInt(2, place);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                quantity = resultSet.getInt("quantity");
            }
        } catch (SQLException e) {
            log.error("Problems with getting count for place/wagon {}", e.toString());
            return false;
        }
        return quantity > 0;
    }


    @Override
    public Optional<List<Ticket>> getAllTicketsById(int id) {
        List<Ticket> ticketsFromDb = new ArrayList<>();
        try (Connection connection = DataBasePoolManager.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_TICKETS_BY_USER_ID)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                ticketsFromDb.add(getTicketFromResultSet(resultSet));
            }
        } catch (SQLException exception) {
            log.error("Problems with getting list of tickets for user {}", exception.toString());
        }
        return Optional.of(ticketsFromDb);
    }

    @Override
    public boolean updateTicketToPaid(int ticketId) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DataBasePoolManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(UPDATE_TICKET_PAID_STATUS);
            connection.setAutoCommit(false);
            preparedStatement.setBoolean(1, true);
            preparedStatement.setInt(2, ticketId);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            log.error("Problems with updating ticket {}", e.toString());
            if (connection != null) {
                try {
                    connection.rollback();
                    return false;
                } catch (SQLException exception) {
                    log.error("Problems with transaction {}", e.toString());
                }
            }
        } finally {
            close(connection);
            close(preparedStatement);
        }
        return true;
    }

    private void createTicketStatement(PreparedStatement preparedStatement, Ticket ticket) throws SQLException {
        preparedStatement.setInt(1, ticket.getId());
        preparedStatement.setInt(2, ticket.getUserId());
        preparedStatement.setInt(3, ticket.getTrain().getId());
        preparedStatement.setString(4, ticket.getFirstName());
        preparedStatement.setString(5, ticket.getLastName());
        preparedStatement.setString(6, ticket.getDepartureStation());
        preparedStatement.setString(7, ticket.getDepartureTime());
        preparedStatement.setString(8, ticket.getArrivalStation());
        preparedStatement.setString(9, ticket.getArrivalTime());
        preparedStatement.setInt(10, ticket.getPlace());
        preparedStatement.setInt(11, ticket.getWagon());
        preparedStatement.setString(12, ticket.getComfortClass());
        preparedStatement.setInt(13, ticket.getPrice());
        preparedStatement.setBoolean(14, ticket.isPaid());
    }

    private Ticket getTicketFromResultSet(ResultSet resultSet) throws SQLException {
        TrainAndModelDao trainAndModelDao = new TrainModelDaoImpl();
        Optional<Train> train = trainAndModelDao.getTrain(resultSet.getInt(3));
        Ticket ticketFromDb = new Ticket();
        ticketFromDb.setId(resultSet.getInt(1));
        ticketFromDb.setUserId(resultSet.getInt(2));
        train.ifPresent(ticketFromDb::setTrain);
        ticketFromDb.setFirstName(resultSet.getString(4));
        ticketFromDb.setLastName(resultSet.getString(5));
        ticketFromDb.setDepartureStation(resultSet.getString(6));
        ticketFromDb.setDepartureTime(resultSet.getString(7));
        ticketFromDb.setArrivalStation(resultSet.getString(8));
        ticketFromDb.setArrivalTime(resultSet.getString(9));
        ticketFromDb.setPlace(resultSet.getInt(10));
        ticketFromDb.setWagon(resultSet.getInt(11));
        ticketFromDb.setComfortClass(resultSet.getString(12));
        ticketFromDb.setPrice(resultSet.getInt(13));
        ticketFromDb.setPaid(resultSet.getBoolean(14));
        return ticketFromDb;
    }

    private static void close(AutoCloseable ac) {
        if (ac != null) {
            try {
                ac.close();
            } catch (Exception e) {
                log.error("Failed closing resource {}", e.toString());
            }
        }
    }
}
