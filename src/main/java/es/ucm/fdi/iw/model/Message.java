package es.ucm.fdi.iw.model;

import javax.persistence.SequenceGenerator;
import java.time.format.DateTimeFormatter;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.ManyToOne;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Data;

/*
 * A message that users can send each other.
 *
 */
@Entity
@NamedQueries({
	@NamedQuery(name="Message.countUnread", query="SELECT COUNT(m) FROM Message m " + "WHERE m.recipient.id = :userId AND m.dateRead = null")
})
@Data
public class Message implements Transferable<Message.Transfer> {
	
	/*
	 *  Id autogenerado que actua como clave primaria de la tabla Message
	 */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gen")
    @SequenceGenerator(name = "gen", sequenceName = "gen")
	private long id;

	@ManyToOne
	private User sender;
	@ManyToOne
	private MessageTopic recipient;

	/*
	 * Contenido del mensaje
	 */
	private String text;

	/*
	 * Fechas de envio y recepcion del mensaje
	 */
	private LocalDateTime dateSent;
	private LocalDateTime dateRead;

	/*
	 * Nombre el equipo del usuario que envia el mensaje
	 */
	private String senderTeamName;

	/*
	 * Bool para indicar si el que envia el mensaje es el usuario
	 */
	private boolean iamSender;

	/**
	 * Objeto para persistir a/de JSON
	 * @author mfreire
	 */
    @Getter
    @AllArgsConstructor
	public static class Transfer {
		private String from;
		private String to;
		private String sent;
		private String received;
		private String text;
		private String fromTeam;
		private boolean iamSender;
		long id;

		public Transfer(Message m) {
			this.from = m.getSender().getUsername();
			this.to = m.getRecipient().getTopicId();
			this.sent = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").format(m.getDateSent());
			this.received = m.getDateRead() == null ? null : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(m.getDateRead());
			this.text = m.getText();
			this.id = m.getId();
			this.fromTeam = m.getSenderTeamName();
			this.iamSender = m.isIamSender();
		}
	}

	@Override
	public Transfer toTransfer() {
		return new Transfer(sender.getUsername(), recipient.getTopicId(), 
			DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").format(dateSent),
			dateRead == null ? null : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateRead),
			text, senderTeamName, iamSender, id);
    }
}
