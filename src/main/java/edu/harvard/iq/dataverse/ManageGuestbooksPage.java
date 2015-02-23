/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.impl.CreateGuestbookCommand;
import edu.harvard.iq.dataverse.engine.command.impl.UpdateDataverseCommand;
import edu.harvard.iq.dataverse.engine.command.impl.UpdateDataverseGuestbookCommand;
import edu.harvard.iq.dataverse.engine.command.impl.UpdateDataverseGuestbookRootCommand;
import edu.harvard.iq.dataverse.util.JsfHelper;
import static edu.harvard.iq.dataverse.util.JsfHelper.JH;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author skraffmiller
 */
@ViewScoped
@Named
public class ManageGuestbooksPage implements java.io.Serializable {

    @EJB
    DataverseServiceBean dvService;

    @EJB
    GuestbookResponseServiceBean guestbookResponseService;
    
    @EJB
    GuestbookServiceBean guestbookService;
    
    @EJB
    EjbDataverseEngine engineService;

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    EntityManager em;

    @Inject
    DataversePage dvpage;

    @Inject
    GuestbookPage guestbookPage;

    @Inject
    DataverseSession session;

    private List<Guestbook> guestbooks;
    private List<GuestbookResponse> responses;
    private Dataverse dataverse;
    private Long dataverseId;
    private boolean inheritGuestbooksValue;

    private Guestbook selectedGuestbook = null;

    public void init() {
        dataverse = dvService.find(dataverseId);
        dvpage.setDataverse(dataverse);

        guestbooks = new LinkedList<>();
        setInheritGuestbooksValue(!dataverse.isGuestbookRoot());
        if (inheritGuestbooksValue && dataverse.getOwner() != null) {
            for (Guestbook pg : dataverse.getParentGuestbooks()) {
                pg.setUsageCount(guestbookService.findCountUsages(pg.getId()));
                pg.setResponseCount(guestbookResponseService.findCountByGuestbookId(pg.getId()));
                pg.setDeletable(true);
                if (!(pg.getUsageCount().intValue() == 0)) {
                    pg.setDeletable(false);
                }
                guestbooks.add(pg);
            }
        }
        for (Guestbook cg : dataverse.getGuestbooks()) {
            cg.setDeletable(true);
            cg.setUsageCount(guestbookService.findCountUsages(cg.getId()));
            if (!(cg.getUsageCount().intValue() == 0)) {
                cg.setDeletable(false);
            }
            cg.setResponseCount(guestbookResponseService.findCountByGuestbookId(cg.getId()));
            cg.setDataverse(dataverse);
            guestbooks.add(cg);
        }
    }




    public void deleteGuestbook() {
        if (selectedGuestbook != null) {
            guestbooks.remove(selectedGuestbook);
            dataverse.getGuestbooks().remove(selectedGuestbook);
            saveDataverse("The guestbook has been deleted.", "The guestbook cannot be deleted");
        } else {
            System.out.print("Selected Guestbook is null");
        }
    }

    public void saveDataverse(ActionEvent e) {
        saveDataverse("", "");
    }
    
    public String enableGuestbook(Guestbook selectedGuestbook) {
        selectedGuestbook.setEnabled(true);
        saveDataverse("The guestbook has been enabled.", "The guestbook could not be enabled.");
        return "";
    }

    public String disableGuestbook(Guestbook selectedGuestbook) {
        selectedGuestbook.setEnabled(false);
        saveDataverse("The guestbook has been disabled.", "The guestbook could not be disabled.");
        return "";
    }
    
    public void viewSelectedGuestbookResponses(Guestbook selectedGuestbook){
        this.selectedGuestbook = selectedGuestbook;
        guestbookPage.setGuestbook(selectedGuestbook);
        setResponses(guestbookResponseService.findAllByGuestbookId(selectedGuestbook.getId()));       
    }

    private void saveDataverse(String successMessage, String failureMessage) {
        if (successMessage.isEmpty()) {
            successMessage = "Dataverse Guestbook data updated";
        }
        if (successMessage.isEmpty()) {
            successMessage = "Dataverse Guestbook data could not be updated.";
        }
        try {
            engineService.submit(new UpdateDataverseCommand(getDataverse(), null, null, session.getUser(), null));
            JsfHelper.addSuccessMessage(successMessage);
        } catch (CommandException ex) {
            JH.addMessage(FacesMessage.SEVERITY_FATAL, failureMessage + ex.getMessage());
        }

    }

    public List<Guestbook> getGuestbooks() {
        return guestbooks;
    }

    public void setGuestbooks(List<Guestbook> guestbooks) {
        this.guestbooks = guestbooks;
    }
    
    
    public List<GuestbookResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<GuestbookResponse> responses) {
        this.responses = responses;
    }

    public Dataverse getDataverse() {
        return dataverse;
    }

    public void setDataverse(Dataverse dataverse) {
        this.dataverse = dataverse;
    }

    public Long getDataverseId() {
        return dataverseId;
    }

    public void setDataverseId(Long dataverseId) {
        this.dataverseId = dataverseId;
    }

    public boolean isInheritGuestbooksValue() {
        return inheritGuestbooksValue;
    }

    public void setInheritGuestbooksValue(boolean inheritGuestbooksValue) {
        this.inheritGuestbooksValue = inheritGuestbooksValue;
    }

    public Guestbook getSelectedGuestbook() {
        return selectedGuestbook;
    }

    public void setSelectedGuestbook(Guestbook selectedGuestbook) {
        this.selectedGuestbook = selectedGuestbook;
    }

    public void viewSelectedGuestbook(Guestbook selectedGuestbook) {
        this.selectedGuestbook = selectedGuestbook;
        guestbookPage.setGuestbook(selectedGuestbook);
    }



    public String updateGuestbooksRoot(javax.faces.event.AjaxBehaviorEvent event) throws javax.faces.event.AbortProcessingException {
        try {
            dataverse = engineService.submit(new UpdateDataverseGuestbookRootCommand(!isInheritGuestbooksValue(), session.getUser(), getDataverse()));
            init();
            return "";
        } catch (CommandException ex) {
            Logger.getLogger(ManageGuestbooksPage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
}
