package de.jee.veranstaltungsverwaltung.model;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import de.jee.veranstaltungsverwaltung.controller.HibernateUtil;

public class NutzerDAO {
	private static final Logger logger = Logger.getLogger(NutzerDAO.class);
	public int save(Nutzer nutzer){
		int returncode = -1;
		nutzer.setBenutzername(nutzer.getBenutzername().toLowerCase());
		EntityManager em = null;
		try{
			em = HibernateUtil.getEntityManager();
			em.getTransaction().begin();
			em.persist(nutzer);
			em.getTransaction().commit();	
			returncode = nutzer.getId();
		}
		catch(Exception e){
			logger.log(Level.DEBUG,"Der Benutzer mit dem Benuternamen: " + nutzer.getBenutzername() + " konnte nicht gespeichert werden:\n" + e.getStackTrace());
		}
		finally{
			em.close();
		}
		return returncode;
	}
	public Nutzer findByID(int id){
		Nutzer nutzer = new Nutzer();
		EntityManager em = null;
		try{
			em = HibernateUtil.getEntityManager();
			nutzer = em.find(Nutzer.class, id);
		}
		catch(NullPointerException np){
			logger.log(Level.INFO, "Es befindet sich kein Benutzer mit der ID: " + id + " in der Datenbank");
		}
		catch(Exception e){
			logger.log(Level.DEBUG, "Der Benutzer mit der ID: " + id + " konnte nicht bezogen werden:\n" + e.getStackTrace());
		}
		finally{
			em.close();
		}
		if(nutzer.getBenutzername() == null)
			return null;
		return nutzer;
	}
	public Nutzer findByUsername(String benutzername){
		benutzername = benutzername.toLowerCase();
		Nutzer nutzer = new Nutzer();
		EntityManager em = null;
		try{
			em = HibernateUtil.getEntityManager();
			CriteriaBuilder cb = HibernateUtil.getCriteriaBuilder();
			CriteriaQuery<Nutzer> query = cb.createQuery(Nutzer.class);
			Root<Nutzer> root = query.from(Nutzer.class);
			query.select(root);
			query.where(cb.equal(root.get("benutzername"), benutzername));
			nutzer = em.createQuery(query).getSingleResult();
		}
		catch(NullPointerException np){
			logger.log(Level.INFO, "Es befindet sich kein Benutzer mit dem Benutzernamen: " + benutzername + " in der Datenbank");
			return null;
		}
		catch(Exception e){
			logger.log(Level.DEBUG, "Der Benutzer mit dem Benutzernamen: " + benutzername + " konnte nicht bezogen werden:\n" + e.getStackTrace());
			return null;
		}
		finally{
			em.close();
		}
		return nutzer;
	}
	
	public boolean changePassword(String benutzername, String altesPasswort, String neuesPasswort){
		benutzername = benutzername.toLowerCase();
		if(!this.checkPassword(benutzername, altesPasswort))
			return false;
		EntityManager em = null;
		try{
			Nutzer nutzer = this.findByUsername(benutzername);
			em = HibernateUtil.getEntityManager();
			em.getTransaction().begin();
			nutzer.setPasswort(neuesPasswort);
			em.refresh(nutzer);
			em.getTransaction().commit();			
		}
		catch(NullPointerException np){
			logger.log(Level.INFO, "Der Benutzer mit dem Benutzernamen: " + benutzername + " konnte nicht bezogen werden");
			return false;
		}
		catch(Exception e){
			logger.log(Level.DEBUG, "Das Passwort konnte nicht aktualisiert werden.\n" + e.getStackTrace());
			return false;
		}
		finally{
			em.close();
		}
		return true;
	}
	public boolean checkPassword(String benutzername, String passwort){
		benutzername = benutzername.toLowerCase();
		try{
			Nutzer nutzer = this.findByUsername(benutzername);
			if(!nutzer.getPasswort().equals(passwort))
				return false;
		}
		catch(NullPointerException np){
			logger.log(Level.INFO, "Der Benutzer mit dem Benutzernamen: " + benutzername + " konnte nicht bezogen werden");
			return false;
		}
		return true;

	}
}
