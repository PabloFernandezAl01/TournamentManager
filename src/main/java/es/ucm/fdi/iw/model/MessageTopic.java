package es.ucm.fdi.iw.model;

import javax.persistence.*;
import java.util.*;
import lombok.Data;

@Entity
@Data
public class MessageTopic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

    @Column(unique=true)
    String topicId;
    
    @OneToMany
    @JoinColumn(name = "recipient_id")
    private List<Message> messages = new ArrayList<>();
}