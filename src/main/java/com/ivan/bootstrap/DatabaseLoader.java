package com.ivan.bootstrap;

import com.ivan.domain.Comment;
import com.ivan.domain.Link;
import com.ivan.domain.Role;
import com.ivan.domain.User;
import com.ivan.repository.CommentRepository;
import com.ivan.repository.LinkRepository;
import com.ivan.repository.RoleRepository;
import com.ivan.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
public class DatabaseLoader implements CommandLineRunner {

    private LinkRepository linkRepository;
    private CommentRepository commentRepository;
    private UserRepository userRepository;
    private RoleRepository roleRepository;

    private Map<String,User> users = new HashMap<>();

    public DatabaseLoader(LinkRepository linkRepository, CommentRepository commentRepository, UserRepository userRepository, RoleRepository roleRepository) {
        this.linkRepository = linkRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {

        // add users and roles
        addUsersAndRoles();

        Map<String,String> links = new HashMap<>();
        links.put("Personal Github","https://github.com/ivancekic");
        links.put("Manjaro Linux Distro","https://manjaro.org/");
        links.put("Spring Boot","https://spring.io/projects/spring-boot");
        

        links.forEach((k,v) -> {
            User u1 = users.get("user@gmail.com");
            User u2 = users.get("super@gmail.com");
            Link link = new Link(k,v);
            if(k.startsWith("Build")) {
                link.setUser(u1);
            } else {
                link.setUser(u2);
            }

            linkRepository.save(link);

            // we will do something with comments later
            Comment spring = new Comment("I love it, great post!",link);
            Comment security = new Comment("This is awesome",link);
            Comment pwa = new Comment("Great post. Thank you!",link);
            Comment comments[] = {spring,security,pwa};
            for(Comment comment : comments) {
                commentRepository.save(comment);
                link.addComment(comment);
            }
        });

        long linkCount = linkRepository.count();
        System.out.println("Number of links in the database: " + linkCount );
    }

    private void addUsersAndRoles() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String secret = "{bcrypt}" + encoder.encode("password");

        Role userRole = new Role("ROLE_USER");
        roleRepository.save(userRole);
        Role adminRole = new Role("ROLE_ADMIN");
        roleRepository.save(adminRole);

        User user = new User("user@gmail.com",secret,true,"Joe","User","joedirt");
        user.addRole(userRole);
        user.setConfirmPassword(secret);
        userRepository.save(user);
        users.put("user@gmail.com",user);

        User admin = new User("admin@gmail.com",secret,true,"Joe","Admin","masteradmin");
        admin.setAlias("joeadmin");
        admin.addRole(adminRole);
        admin.setConfirmPassword(secret);
        userRepository.save(admin);
        users.put("admin@gmail.com",admin);

        User master = new User("super@gmail.com",secret,true,"Super","User","superduper");
        master.addRoles(new HashSet<>(Arrays.asList(userRole,adminRole)));
        master.setConfirmPassword(secret);
        userRepository.save(master);
        users.put("super@gmail.com",master);
    }

}
