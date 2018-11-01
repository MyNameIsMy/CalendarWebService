package org.suchushin.projects.service.data;

import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

@Transactional
interface UserDAO extends CrudRepository<User, Integer> {
    void deleteByLogin(String login);

    User findByLogin(String login);
}
