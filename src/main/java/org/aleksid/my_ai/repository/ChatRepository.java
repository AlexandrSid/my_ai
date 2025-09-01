package org.aleksid.my_ai.repository;

import org.aleksid.my_ai.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long>{
}
