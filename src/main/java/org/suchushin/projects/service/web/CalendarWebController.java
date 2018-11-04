package org.suchushin.projects.service.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.suchushin.projects.service.data.DataController;
import org.suchushin.projects.service.data.Event;
import org.suchushin.projects.service.data.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;


@RestController
public class CalendarWebController {

    private static String HELLO_TEXT = "<p>Hello! You visit Online Calendar Web-Service Hello-Page. <br />" +
            "You will find information for further service using there.<br />" +
            "All of urls and their descriptions are listed below:<br />" +
            "/registration - registration of new user, your request type must be POST type and body must contain json object with 'login' and 'password' properties;<br />" +
            "/login - login user; <br />" +
            "/user/events - get user events, your request type must be GET type;<br />" +
            "/user/events-by-name - getting user events by name of event where 'name' must be request parameter, your request type must be GET type;<br />" +
            "/user/events-by-date - getting user events by date of event where 'date' must be request parameter(date format: 'yyyy-mm-dd'), your request type must be GET type;<br />" +
            "/user/save-event - saving new event, your request type must be POST type and body must contain json object with 'eventName', 'eventDescription' and 'eventDate' properties(date format: 'yyyy-mm-dd');<br />" +
            "/user/delete-event - deleting event(or events), your request type must be DELETE type and body must contain json object with 'name', 'description' and 'date' properties(date format: 'yyyy-mm-dd'; if you don't want specify some parameter, specify their values as 'null');<br />" +
            "/user/delete-user - suicide)))).</p>";


    private static String ROLE_USER = "ROLE_USER";

    @Autowired
    private DataController controller;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView redirectToHelloPage(ModelMap model){
        return new ModelAndView("redirect:/hello-page", model);
    }

    @RequestMapping(value = "/hello-page", method = RequestMethod.GET)
    public ResponseEntity<String> helloPage(){
        return new ResponseEntity<>(HELLO_TEXT, HttpStatus.OK);
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ResponseEntity<String> registerUser(@RequestBody RegistrationForm form){
        if (controller.insertUser(form.getLogin(), passwordEncoder.encode(form.getPassword()), ROLE_USER, true))
            if (controller.createCalendarForNewUser(form.getLogin()))
                return new ResponseEntity<>("Registration successful", HttpStatus.OK);
            else
                controller.deleteUser(form.getLogin());
        return new ResponseEntity<>("Registration failed...", HttpStatus.NOT_MODIFIED);
    }

    @RequestMapping(value = "/admin/user", method = RequestMethod.GET)
    public ResponseEntity<User> getUser(@RequestParam String login){
        return new ResponseEntity<>(controller.retrieveUser(login), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/events", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getUserEvents(Principal principal){
        return new ResponseEntity<>(controller.retrieveCalendarEvents(principal.getName()), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/events-by-name", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getUserEventsByName(Principal principal, @RequestParam("name") String name){
        return new ResponseEntity<>(controller.retrieveCalendarEventsByName(principal.getName(), name), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/events-by-date", method = RequestMethod.GET)
    public ResponseEntity<List<Event>> getUserEventsByDate(Principal principal, @RequestParam("date") String date){
        return new ResponseEntity<>(controller.retrieveCalendarEventsByDate(principal.getName(), date), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/save-event", method = RequestMethod.POST)
    public ResponseEntity<String> saveUserEvent(Principal principal, @RequestBody Event event){
        if (event.getEventName() == null || event.getEventDescription() == null || event.getEventDate() == null)
            return new ResponseEntity<>("'null' value deprecated", HttpStatus.OK);
        if (controller.insertEvent(principal.getName(), event.getEventName(), event.getEventDescription(), event.getEventDate()))
            return new ResponseEntity<>("Insertion successful", HttpStatus.OK);
        return new ResponseEntity<>("Insertion failed...", HttpStatus.NOT_MODIFIED);
    }

    @RequestMapping(value = "/user/delete-event", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteUserEvent(Principal principal, @RequestBody Event event){
        if (controller.deleteEvent(principal.getName(), event.getEventName(), event.getEventDescription(), event.getEventDate()))
            return new ResponseEntity<>("Deletion successful", HttpStatus.OK);
        return new ResponseEntity<>("Deletion failed...", HttpStatus.NOT_MODIFIED);
    }

    @RequestMapping(value="/user/delete-user", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteUser(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
        User user = controller.retrieveUser(auth.getName());

        if (controller.deleteUser(auth.getName())) {
            if (controller.deleteUserCalendar(auth.getName())) {
                try {
                    new SecurityContextLogoutHandler().logout(request, response, auth);
                } catch (Exception e) {
                    if (controller.insertUser(user.getLogin(), user.getPassword(), ROLE_USER, true))
                        return new ResponseEntity<>("User isn't deleted", HttpStatus.NOT_MODIFIED);
                }
            } else {
                if (controller.insertUser(user.getLogin(), user.getPassword(), ROLE_USER, true))
                    return new ResponseEntity<>("User isn't deleted", HttpStatus.NOT_MODIFIED);
            }
        }

        return new ResponseEntity<>("Deletion successful", HttpStatus.OK);
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ResponseEntity<String> loginUser(){
        return new ResponseEntity<>("Success login", HttpStatus.OK);
    }

}
