package com.aatout.web;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.aatout.dao.CompteRepository;
import com.aatout.dao.DepotRepository;
import com.aatout.dao.OperationAutorisationDao;
import com.aatout.dao.OperationRepository;
import com.aatout.dao.RetraitRepository;
import com.aatout.enums.StatusName;
import com.aatout.model.Compte;
import com.aatout.model.Depot;
import com.aatout.model.Operation;
import com.aatout.model.OperationAutorisation;
import com.aatout.model.Retrait;
import com.aatout.operation.OperationService;
import com.aatout.payload.OperationForm;

@RestController
@CrossOrigin("*")
@RequestMapping("/operation")
public class OperationRestController {
	@Autowired
	private OperationRepository operationRepository; 
	
	@Autowired
	private DepotRepository depotRepository;

	@Autowired
	private RetraitRepository retraitRepository;
	
	@Autowired
	private OperationService operationService;
	
	@Autowired
	private OperationAutorisationDao opAutoDao;

	@Autowired
	private CompteRepository compteRepository;


	@GetMapping(value="/list")
	public List<Operation> getOperations(){
		return operationRepository.findAll();
		//return operationRepository.findBySuprIsFalse();
	}
	
	@GetMapping(value="/list-en")
	public List<OperationAutorisation> getOperationsEn(){
		return opAutoDao.findAll();
		//return operationRepository.findBySuprIsFalse();
	}

	@GetMapping(value="/list/{id}")
	public Operation getOperation(@PathVariable Long id){

		//return operationRepository.findOne(id);
		Operation uneOperation = operationRepository.findBySuprIsFalseAndId(id);

		return uneOperation;
	}
	
	@GetMapping(value="/mes-operation/{username}")
	public List<Operation> getMesOperation(@PathVariable String username){
		return operationRepository.findBySuprAndAutorisedBy(false, username);
	}	

	@GetMapping(value="/list-depot")
	public List<Depot> getDepot(){
		//return operationRepository.findOne(id);

		return depotRepository.findBySuprIsFalse();
	}


	@GetMapping(value="/list-retrait")
	public List<Retrait> getRetrait(){

		//return operationRepository.findOne(id);

		return retraitRepository.findBySuprIsFalse();
	}



	//Methodes pour obtenir la Liste des comptes monnaie 

	@GetMapping(value = "/consulter-compte/{numCompte}")
	public Compte getCompte(@PathVariable String numCompte){
		return compteRepository.findCompteByActiveIsTrueAndSuprFalseAndFermerIsFalseAndNumCompte(numCompte);
	}


	@PostMapping(value= "/save")
	public boolean  saveOperation(@RequestBody FormOperation operation){ 

		System.out.println("foutou---->"+operation );



		//Recherche un compte vie le numCompte donné par operation		
		Compte compte = getCompte(operation.getUnCompte().getNumCompte());

		System.out.println("*******kkkkkkk ==>>>: "+compte);


		System.out.println(compte);

		if(operation.getType().equals("DEPO")) {
			try {
				System.out.println("*******ppppp ==>>>: "+compte);
				//operationService.verser(compte , compte.getSolde() );
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

		}




		/*try  { 

			if(operation.getType().equals("RETR" )) 

				operationService.retirer(compte, compte.getSolde()); 


			else if(operation.getType().equals("DEPO")) 

				operationService.verser(compte , compte.getSolde() ); 
				//operationMetierImpl.depot(depot);

			else  if (operation.getType().equals("Virement")) 

				operationService.virement(compte, compte, compte.getSolde());

		} catch  (Exception e) { 
			//return  "redirect:/consultercompte?codeCte=" +foutou.getNumCompte() +"&error=" +e.getMessage();  

		} 
		 */
		return   true;
		//return (Operation)foutou;
	}


	//edit depot
	@RequestMapping(value="/depot/{id}",method=RequestMethod.PUT)
	public Operation saveOperation(@PathVariable Long id,@RequestBody Depot c){

		Operation op= operationRepository.findBySuprIsFalseAndId(id);			
		op.getCompte().setSolde(op.getCompte().getSolde() - op.getMontantOp() + c.getMontantOp());
		compteRepository.save(op.getCompte());

		c.setId(id);
		return operationRepository.save(c);
	}
	//edit retrait
	@RequestMapping(value="/retrait/{id}",method=RequestMethod.PUT)
	public Operation saveOperation(@PathVariable Long id,@RequestBody Retrait c){

		Operation op= operationRepository.findBySuprIsFalseAndId(id);			
		op.getCompte().setSolde(op.getCompte().getSolde() + op.getMontantOp() - c.getMontantOp());
		compteRepository.save(op.getCompte());

		c.setId(id);
		return operationRepository.save(c);
	}


	@RequestMapping(value="/depot/{id}",method=RequestMethod.DELETE)
	public boolean supprimerDepot(@PathVariable Long id){

		try {
			Operation op= operationRepository.findBySuprIsFalseAndId(id);			
			op.getCompte().setSolde(op.getCompte().getSolde() - op.getMontantOp());
			compteRepository.save(op.getCompte());

			op.setSupr(true);
			operationRepository.save(op);
			//operationRepository.delete(id);
			return true;
		} catch (Exception e) {
			return false;
		}

	}


	@RequestMapping(value="/retrait/{id}",method=RequestMethod.DELETE)
	public boolean supprimerRetrait(@PathVariable Long id){

		try {
			Operation op= operationRepository.findBySuprIsFalseAndId(id);			
			op.getCompte().setSolde(op.getCompte().getSolde() + op.getMontantOp());
			compteRepository.save(op.getCompte());

			op.setSupr(true);
			operationRepository.save(op);
			//operationRepository.delete(id);
			return true;
		} catch (Exception e) {
			return false;
		}

	}


	@Transactional
	@PostMapping(value="/save-depot")
	public boolean saveDepotd(@RequestBody Operation operation) throws RemoteException { 

		System.out.println("Mon depot ======>>>: "+operation);

		Operation uneOperation = operationRepository.findBySuprIsFalseAndId(operation.getId());
		

		if (operation instanceof Depot && uneOperation == null) {


			Depot unDepot = (Depot) operation;


			unDepot.getCompte().setSolde(unDepot.getCompte().getSolde() + unDepot.getMontantOp());

			compteRepository.save(unDepot.getCompte());
			//operationService.verser(unDepot);


			operationRepository.saveAndFlush(unDepot);

		}

		if (operation instanceof Retrait) {

			Retrait unRetrait = (Retrait) operation;
			
			unRetrait.getCompte().setSolde(unRetrait.getCompte().getSolde() - unRetrait.getMontantOp());
			
			compteRepository.save(unRetrait.getCompte());  

			operationRepository.saveAndFlush(unRetrait);

		}

		return true;
	}
	
	@PostMapping(value="/save-operation")
	public OperationForm saveOperation(@RequestBody OperationForm operationForm){
		System.out.println("-----------------------");
		System.out.println(operationForm);
		System.out.println("=====-------=====");
		System.out.println(operationForm.getNumCompte());
		System.out.println(operationForm.getId());
		System.out.println(operationForm.getMontantOp());
		System.out.println(operationForm.getNumCompte2());
		System.out.println("=====---TYPE----=====");
		System.out.println(operationForm.getType());
		System.out.println(operationForm.getBadge());
		/*System.out.println(operationForm);
		//Random rcd = new Random();
		Long idMax =  operationRepository.getMax();
		
		System.out.println(idMax); 
		
		Operation operation =  operationRepository.findBySuprIsFalseAndId(idMax);
		
		Long badge = 0L;
		
		if (operation == null ) {
			badge = 1001L;
		}else {
			badge = operation.getBadge() + 1;
		}*/
		
		try{ 
			if(operationForm.getType().equals("DEPO")){
				operationService.augmenter(operationForm.getNumCompte(), operationForm.getNumCompte2(), operationForm.getMontantOp(), operationForm.getNarrative(), operationForm.getBadge(), operationForm.getCreateBy(), operationForm.getAutorisedBy());
			}
			else if(operationForm.getType().equals("RETR")){
				operationService.diminuer(operationForm.getNumCompte(), operationForm.getNumCompte2(), operationForm.getMontantOp(), operationForm.getNarrative(), operationForm.getBadge(), operationForm.getCreateBy(), operationForm.getAutorisedBy());
			} 
			if (operationForm.getType().equals("VIRE")){
				operationService.virement(operationForm.getNumCompte(), operationForm.getNumCompte2(), operationForm.getMontantOp(),  operationForm.getNarrative(), operationForm.getBadge(), operationForm.getCreateBy(), operationForm.getAutorisedBy());
			}
		} catch(Exception e) {
			//model.addAttribute("error", e);
			/*return "redirect:/consulterCompte?operationForm.getNumCompte()="+operationForm.getNumCompte()+
					"&error="+e.getMessage();*/
			return null;
		}
		
		/*return "redirect:/consulterCompte?operationForm.getNumCompte()="+operationForm.getNumCompte();*/
		return operationForm;
	}
	
	
	@PostMapping(value="/save-operation-virement")
	public OperationForm saveOperationVirement(@RequestBody OperationForm operationForm){
		
		/*System.out.println(operationForm);
		//Random rcd = new Random();
		Long idMax =  operationRepository.getMax();
		
		System.out.println(idMax); 
		
		Operation operation =  operationRepository.findBySuprIsFalseAndId(idMax);
		
		Long badge = 0L;
		
		if (operation == null ) {
			badge = 1001L;
		}else {
			badge = operation.getBadge() + 1;
		}*/
		
		try{  
			if (operationForm.getType().equals("VIRE")){
				operationService.virement(operationForm.getNumCompte(), operationForm.getNumCompte2(), operationForm.getMontantOp(),  operationForm.getNarrative(), operationForm.getBadge(), operationForm.getCreateBy(), operationForm.getAutorisedBy());
			}
		} catch(Exception e) {
			//model.addAttribute("error", e);
			/*return "redirect:/consulterCompte?operationForm.getNumCompte()="+operationForm.getNumCompte()+
					"&error="+e.getMessage();*/
			return null;
		}
		
		/*return "redirect:/consulterCompte?operationForm.getNumCompte()="+operationForm.getNumCompte();*/
		return operationForm;
	}
	
	
	/*@RequestMapping(value="/save-operation", method=RequestMethod.POST)
	public OperationAutorisation saveOperation(@RequestBody OperationAutorisation operationForm){
		System.out.println(operationForm);
		try{
			if(operationForm.getType().equals("DEPO")){
				operationService.verser(operationForm.getCompte().getNumCompte(), operationForm.getMontantOp(), operationForm.getNarrative() );
			}
			else if(operationForm.getType().equals("RETR")){
				operationService.retirer(operationForm.getCompte().getNumCompte(), operationForm.getMontantOp(), operationForm.getNarrative());
			} 
			if (operationForm.getType().equals("VIRE")){
				operationService.virement(operationForm.getCompte().getNumCompte(), operationForm.getCompte().getNumCompte(), operationForm.getMontantOp(),  operationForm.getNarrative());
			}
		} catch(Exception e) {
			//model.addAttribute("error", e);
			return "redirect:/consulterCompte?operationForm.getNumCompte()="+operationForm.getNumCompte()+
					"&error="+e.getMessage();
			return null;
		}
		
		return "redirect:/consulterCompte?operationForm.getNumCompte()="+operationForm.getNumCompte();
		return operationForm;
	}*/
	
	@PostMapping(value="/save-operation-autorisation")
	public OperationForm saveOperationAuto(@RequestBody OperationForm operationForm){
		
		Long idMax =  opAutoDao.getMaxEn();
		
		System.out.println(idMax);
		
		OperationAutorisation operation =  opAutoDao.findByStatusIsFalseAndSuprUserIsFalseAndId(idMax);
		
		Long badge = 0L;
		
		if (operation == null ) {
			badge = 1001L;
		}else {
			badge = operation.getBadge() + 1;
		}
		try{
			if(operationForm.getType().equals("DEPO")){
				
				operationService.verserEn(operationForm.getNumCompte(), operationForm.getNumCompte2(), operationForm.getMontantOp(), badge, operationForm.getNarrative(), operationForm.getCreateBy());
			}
			else if(operationForm.getType().equals("RETR")){
				operationService.retirerEn(operationForm.getNumCompte(), operationForm.getNumCompte2(), operationForm.getMontantOp(), badge, operationForm.getNarrative(), operationForm.getCreateBy());
			} 
			if (operationForm.getType().equals("VIRE")){
				operationService.virementEn(operationForm.getNumCompte(), operationForm.getNumCompte2(), operationForm.getMontantOp(),  badge, operationForm.getNarrative(), operationForm.getCreateBy());
			}
		} catch(Exception e) {
			//model.addAttribute("error", e);
			/*return "redirect:/consulterCompte?operationForm.getNumCompte()="+operationForm.getNumCompte()+
					"&error="+e.getMessage();*/
			return null;
		}
		
		/*return "redirect:/consulterCompte?operationForm.getNumCompte()="+operationForm.getNumCompte();*/
		return operationForm;
	}
	
	@PostMapping(value="/accepter-operation")
	public OperationAutorisation accepterOperation(@RequestBody OperationAutorisation operationAutorisation){
		OperationAutorisation op = opAutoDao.findByStatusIsFalseAndSuprUserIsFalseAndId(operationAutorisation.getId());
		op.setEtat(StatusName.AUTORISEE);
		op.setAutorisedBy(operationAutorisation.getAutorisedBy());
		return opAutoDao.saveAndFlush(op);
	}
	
	@PostMapping(value="/rejeter-operation")
	public OperationAutorisation rejeterOperation(@RequestBody OperationAutorisation operationAutorisation){
		OperationAutorisation op = opAutoDao.findByStatusIsFalseAndSuprUserIsFalseAndId(operationAutorisation.getId());
		op.setEtat(StatusName.REJETEE);
		op.setAutorisedBy(operationAutorisation.getAutorisedBy());
		return opAutoDao.saveAndFlush(op);
	}
	
	@PostMapping(value="/valide-operation")
	public OperationAutorisation finOperation(@RequestBody OperationAutorisation operationAutorisation){
		OperationAutorisation op = opAutoDao.findByStatusIsFalseAndSuprUserIsFalseAndId(operationAutorisation.getId());
		op.setEtat(StatusName.VALIDEE);
		return opAutoDao.saveAndFlush(op); 
	}
	
	//@@@@@@ Recupération des opérations par Date @@@@//
	
	@GetMapping(value="/mes-operation-by-date-between/{dateOp}")
	public List<Operation> getMesOperationByDateBetween(@PathVariable Date[] dateOp){
		return operationRepository.findBySuprIsFalseAndDateOpBetween(dateOp[0], dateOp[1]);
	}
	
	@GetMapping(value="/mes-operation-by-date/{dateOp}")
	public List<Operation> getMesOperationByDate(@PathVariable Date dateOp){
		return operationRepository.findBySuprIsFalseAndDateOp(dateOp);
	}
	
	@GetMapping(value="/mes-operation-by-date-greater-than/{dateOp}")
	public List<Operation> getMesOperationByDateGreaterThan(@PathVariable Date dateOp){
		return operationRepository.findBySuprIsFalseAndDateOpGreaterThan(dateOp);
	}
	
	@GetMapping(value="/mes-operation-by-date-less-than/{dateOp}")
	public List<Operation> getMesOperationByDateLessThan(@PathVariable Date dateOp){
		return operationRepository.findBySuprIsFalseAndDateOpLessThan(dateOp);
	}
	
	
	

}
