package Final_Project;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class GeneDB {
  /**
   * Run this method to access the database and it's functionality functionality.
   */
  public static void main(String[] args) {
    GeneDB db = new GeneDB();
    Connection con = db.login();
    db.run(con);
  }

  /**
   * Establishes a connection to the diseaseGenomics database using the inputted user and password
   * details. Used within void login() method.
   */
  private Connection getConnection(String user, String pass) throws SQLException {
    Connection conn = null;
    Properties connectionProps = new Properties();
    connectionProps.put("user", user);
    connectionProps.put("password", pass);

    conn = DriverManager.getConnection("jdbc:mysql://"
            + "localhost" + ":" + 3306 + "/" + "diseaseGenomics",
        connectionProps);

    return conn;
  }

  /**
   * Prompts the user to input the a username and password in order to connect, repeatedly asks user
   * to sign in until a valid connect is established.
   *
   * NOTE- if connection fails despite inputting correct credentials paste and run these two lines
   * in MySQL: SET @@global.time_zone = '+00:00'; SET @@session.time_zone = '+00:00';
   */
  private Connection login() {
    boolean passed = false;
    String user = null;
    String pass = null;
    Connection conn = null;
    while (!passed) {
      Scanner s = new Scanner(System.in);
      System.out.println("Provide MySQL username:");
      user = s.next();
      System.out.println("Provide MySQL password:");
      pass = s.next();
      try {
        conn = getConnection(user, pass);
        System.out.println("Connected to server\n");
        passed = true;
      } catch (SQLException e) {
        System.out.println("ERROR: Could not connect to the database \n"
            + e.getMessage());
      }
    }
    return conn;
  }

  /**
   * Manages the main page of the program and accepts user commands.
   *
   * @param con Connection to the diseaseGenomics database
   */
  public void run(Connection con) {
    Scanner scan = new Scanner(System.in);
    System.out.println("Welcome to Nathan's Organism Database!\n"
        + "This is the main page\n"
        + "For help simply write 'help'");
    while (true) {
      String command = scan.next().toLowerCase();
      switch (command) {
        case "q":
        case "quit":
          System.out.println("Disconnecting ....");
          tryToCloseConnection(con);
          System.out.println("Bye!");
          return;
        case "help":
          showCommands();
          break;
        case "view":
          showAll(scan, con);
          break;
        case "viewsubset":
          showSubset(scan, con);
          break;
        case "writeto":
          writeTo(scan, con);
          break;
        case "delete":
          deleteRow(scan, con);
          break;
        case "modify":
          modifyRow(scan, con);
          break;
        default:
          System.out.println("Invalid input. Write 'help' if you need help!\n");
          break;
      }
      System.out.println("\nThis is the main page.\n" + "For help simply write 'help'");
      ;
    }
  }

  private void modifyRow(Scanner scan, Connection con) {
    String table = scan.next();
    switch (table) {
      case "proteins":
        handleProtModify(scan, con);
        break;
      case "organism_diseases":
        handleOrgDisModify(scan, con);
        break;
      case "organism_organs":
        handleOrgOrgsModify(scan, con);
        break;
      default:
        System.out.println("The inputted table can't be modified or doesn't exist");
        return;
    }
    return;
  }

  private void handleOrgOrgsModify(Scanner scan, Connection con) {
    ArrayList<String> existingOrganisms = getExistingOrganisms(con);
    ArrayList<String> existingOrgans = getExistingOrgans(con);
    System.out.println("old organism name to be modified");
    System.out.println("options:");
    System.out.println(existingOrganisms);
    String oldOrganism = scan.next();
    if (!existingOrganisms.contains(oldOrganism)) {
      System.out.println("Cant modify information about a nonrecorded organism");
      return;
    }
    System.out.println("old organ to be modified");
    System.out.println("options:");
    System.out.println(existingOrgans);
    String oldOrg = scan.next();
    if (!existingOrgans.contains(oldOrg)) {
      System.out.println("Cant modify information about a nonrecorded organ");
      return;
    }
    System.out.println("new organism to be added");
    System.out.println("options:");
    System.out.println(existingOrganisms);
    String newOrganism = scan.next();
    if (!existingOrganisms.contains(oldOrganism)) {
      System.out.println("Cant add information about a nonrecorded organism");
      return;
    }
    System.out.println("new organ to be added");
    System.out.println("options:");
    System.out.println(existingOrgans);
    String newOrgan = scan.next();
    if (!existingOrgans.contains(newOrgan)) {
      System.out.println("Cant add information about a nonrecorded organ");
      return;
    }
    CallableStatement statement = tryPrepareCall(con,
        "{call modify_orgOrgs_row( ?, ? ,? , ? )}");
    tryToSetString(1, oldOrganism, statement);
    tryToSetString(2, oldOrg, statement);
    tryToSetString(3, newOrganism, statement);
    tryToSetString(4, newOrgan, statement);
    System.out.println("Modification successful!");
  }


  private void handleOrgDisModify(Scanner scan, Connection con) {
    ArrayList<String> existingDiseases = getExistingDiseases(con);
    ArrayList<String> existingOrganisms = getExistingOrganisms(con);
    System.out.println("name of organism to be modified");
    System.out.println("options:");
    System.out.println(existingOrganisms);
    String oldOrg = scan.next();
    if (!existingOrganisms.contains(oldOrg)) {
      System.out.println("Cannot modify data of a nonrecorded organism");
      return;
    }
    System.out.println("name of disease to be modified");
    System.out.println("options:");
    System.out.println(existingDiseases);
    String oldDis = scan.next();
    if (!existingDiseases.contains(oldDis)) {
      System.out.println("Cannot modify data of a nonrecorded disease");
      return;
    }
    System.out.println("new name of organism");
    System.out.println("options:");
    System.out.println(existingOrganisms);
    String newOrg = scan.next();
    if (!existingOrganisms.contains(newOrg)) {
      System.out.println("Cant add info about a nonrecorded organism");
      return;
    }
    System.out.println("new disease for organism");
    System.out.println("options:");
    System.out.println(existingDiseases);
    String newDis = scan.next();
    if (!existingDiseases.contains(newDis)) {
      System.out.println("Cant add info about a nonrecorded disease");
      return;
    }
    CallableStatement statement = tryPrepareCall(con,
        "{call modify_orgDis_row( ?, ? ,? , ? )}");
    tryToSetString(1, oldOrg, statement);
    tryToSetString(2, oldDis, statement);
    tryToSetString(3, newOrg, statement);
    tryToSetString(4, newDis, statement);
    tryToExecuteQuery(statement);
    System.out.println("Modification successful!");
  }

  private void handleProtModify(Scanner scan, Connection con) {
    System.out.println("id of protein to be modified (must be one of the ones below) (int)");
    System.out.println("options:");
    ArrayList<String> existingProts = getExistingValues(con, "protein_id", "proteins");
    System.out.println(existingProts);
    String oldProtId = scan.next();
    if (!existingProts.contains(oldProtId)) {
      System.out.println("You can't modify a non-recorded protein!");
      return;
    }
    System.out.println("new protein_id (int)");
    String newProtId = scan.next();
    if (existingProts.contains(newProtId)) {
      System.out.println("A protein with that ID already exists!");
      return;
    }
    System.out.println("new associated organ (must be one of the ones below) (String)");
    System.out.println("options:");
    ArrayList<String> existingOrgans = getExistingValues(con, "organName", "organs");
    System.out.println(existingOrgans);
    String newOrgan = scan.next();
    if (!existingOrgans.contains(newOrgan)) {
      System.out.println("Your new protein must be associated with an existing organ");
      return;
    }
    System.out.println("new associated gene (must be one of the ones below) (String)");
    System.out.println("options");
    ArrayList<String> existingGenes = getExistingValues(con, "gene_id", "genes");
    System.out.println(existingGenes);
    String newGene = scan.next();
    if (!existingGenes.contains(newGene)) {
      System.out.println("Your new protein must be associated with an existing gene");
      return;
    }
    System.out.println("Size in Amino Acids");
    String newSize = scan.next();
    System.out.println("the new name of your protein");
    String newName = scan.next();
    if (existingGenes.contains(newName)) {
      System.out.println("The is already a gene with that name!");
      return;
    }
    CallableStatement statement = tryPrepareCall(con,
        "{call modify_proteins_row( ?, ? ,? , ? , ? , ? )}");
    tryToSetString(1, oldProtId, statement);
    tryToSetString(2, newProtId, statement);
    tryToSetString(3, newOrgan, statement);
    tryToSetString(4, newGene, statement);
    tryToSetString(5, newSize, statement);
    tryToSetString(6, newName, statement);
    tryToExecuteQuery(statement);
    System.out.println("Modification successful!");
  }


  private void deleteRow(Scanner scan, Connection con) {
    String table = scan.next();
    switch (table) {
      case "proteins":
        handleProtDelete(scan, con);
        break;
      case "organism_diseases":
        handleOrgDisDelete(scan, con);
        break;
      case "organism_organs":
        handleOrgOrgsDelete(scan, con);
      default:
        System.out.println("You cant delete from the given table! Try again!");
        return;
    }
    return;
  }

  private void handleProtDelete(Scanner scan, Connection con) {
    System.out.println("To delete a protein provide the protein ID");
    ArrayList<String> existingProts = getExistingValues(con, "protein_id", "proteins");
    System.out.println("options:");
    System.out.println(existingProts);
    String protID = scan.next();
    if (!existingProts.contains(protID)) {
      System.out.println("You cant delete a protein that doesn't exist!");
      return;
    } else {
      CallableStatement statement = tryPrepareCall(con,
          "{call drop_row( 'proteins', 'protein_id' , ? )}");
      tryToSetString(1, protID, statement);
      tryToExecuteQuery(statement);
      System.out.println("Delete successful!");
      return;
    }
  }


  private void handleOrgDisDelete(Scanner scan, Connection con) {
    System.out.println("To delete all disease records associated with a given organism "
        + "provide the organism's name exactly as listed below:");
    ArrayList<String> existingOrganisms = getExistingValues(con, "common_name", "organisms");
    System.out.println("options:");
    System.out.println(existingOrganisms);
    String organism = scan.next();
    if (!existingOrganisms.contains(organism)) {
      System.out.println("You cant delete records associated with a non-recorded organism"
          + "!");
      return;
    } else {
      CallableStatement statement = tryPrepareCall(con,
          "{call drop_row( 'organism_diseases', 'organism' , ? )}");
      tryToSetString(1, organism, statement);
      tryToExecuteQuery(statement);
      System.out.println("Delete successful!");
      return;
    }
  }

  private void handleOrgOrgsDelete(Scanner scan, Connection con) {
    System.out.println("To delete all organ records associated with a given organism "
        + "provide the organism's name exactly as listed below:");
    ArrayList<String> existingOrganisms = getExistingValues(con, "common_name", "organisms");
    System.out.println("options:");
    System.out.println(existingOrganisms);
    String organism = scan.next();
    if (!existingOrganisms.contains(organism)) {
      System.out.println("You cant delete records associated with a non-recorded organism!"
          + "!");
      return;
    } else {
      CallableStatement statement = tryPrepareCall(con,
          "{call drop_row( 'organism_organs', 'organism' , ? )}");
      tryToSetString(1, organism, statement);
      tryToExecuteQuery(statement);
      System.out.println("Delete successful!");
      return;
    }
  }


  /**
   * Allows user to write to the organisms, genes or diseases tables within the database.
   *
   * @param scan User input Scanner
   * @param con Connection to diseaseGenomics DB
   */
  private void writeTo(Scanner scan, Connection con) {
    String table = scan.next();
    switch (table) {
      case "organisms":
        handleOrganismWrite(scan, con);
        break;
      case "genes":
        handleGeneWrite(scan, con);
        break;
      case "diseases":
        handleDiseaseWrite(scan, con);
        break;
      default:
        System.out.println("The table you attempt to write to either cant be written to or doesnt"
            + "exist!");
    }
  }


  /**
   * Allows user to write to the organisms table.
   *
   * @param scan User input Scanner
   * @param con Connection to diseaseGenomics DB
   */
  private void handleOrganismWrite(Scanner scan, Connection con) {
    System.out.println("To add an organism specify the arguments in this order:\n"
        + " genus_name(String), species_name(String), common_name(Continous String), "
        + "chromosome_count(int),"
        + " lifespan_in_days(int)");
    ArrayList<String> currentOrganisms = getExistingOrganisms(con);
    String genus = scan.next();
    String species = scan.next();
    String common_name = scan.next();
    String chr = scan.next();
    String life = scan.next();
    if (currentOrganisms.contains(common_name)) {
      System.out.println("You must add a novel organism, the common name must be unique!");
      return;
    } else {
      CallableStatement statement = tryPrepareCall(con,
          "{call addToOrganisms( ? , ? , ? , ? , ?)}");
      tryToSetString(1, genus, statement);
      tryToSetString(2, species, statement);
      tryToSetString(3, common_name, statement);
      tryToSetString(4, chr, statement);
      tryToSetString(5, life, statement);
      endWrite(statement);
    }
  }

  /**
   * Allows user to write to the genes table
   *
   * @param scan User input Scanner
   * @param con Connection to diseaseGenomics DB
   */
  private void handleGeneWrite(Scanner scan, Connection con) {
    System.out.println("To add a gene to the database specify the arguments in this order:\n"
        + "-Organism common name (must exist in organisms table) (String)\n"
        + "-UniqueGeneID (String) (must NOT exist in the genes table)\n"
        + "-Gene_locus (int)\n"
        + "-Chromosome number (int)\n"
        + "-SizeOfGene (in Basepairs) (int)\n");
    ArrayList<String> currentGenes = getExistingGenes(con);
    ArrayList<String> currentOrganisms = getExistingOrganisms(con);
    System.out.println("Existing organisms: " + currentOrganisms);
    System.out.println("Existing genes: " + currentGenes);
    String organism = scan.next();
    if (!currentOrganisms.contains(organism)) {
      System.out
          .println("The given organism must exist in the organisms table. If you'd like to \"\n"
              + "          + \"add genes for a non recorded organism add it to the organisms table first!");
      return;
    }
    String geneID = scan.next();
    if (currentGenes.contains(geneID)) {
      System.out.println("Must have a GeneID that doesnt yet exist!");
      return;
    }
    String gene_locus = scan.next();
    String chrNum = scan.next();
    String geneSize = scan.next();
    CallableStatement statement = tryPrepareCall(con, "{call addToGenes( ? , ? , ? , ? , ?)}");
    tryToSetString(1, organism, statement);
    tryToSetString(2, geneID, statement);
    tryToSetString(3, gene_locus, statement);
    tryToSetString(4, chrNum, statement);
    tryToSetString(5, geneSize, statement);
    endWrite(statement);
  }

  /**
   * allows users to write to diseases table.
   *
   * @param scan User input Scanner
   * @param con Connection to diseaseGenomics DB
   */
  private void handleDiseaseWrite(Scanner scan, Connection con) {
    System.out.println("To add a disease to the database specify the arguments in this order:\n"
        + "Unique Disease name (continuous String up to 30chars, please represent spaces as '_')\n "
        + "Web Link to further resources (String) ");
    ArrayList<String> currentDiseases
        = getExistingValues(con, "common_name", "organisms");
    String diseaseName = scan.next();
    if (currentDiseases.contains(diseaseName)) {
      System.out.println("The inputted disease must not exist in the database, try again");
      return;
    } else {
      String link = scan.next();
      CallableStatement statement = tryPrepareCall(con, "{call addToDiseases( ? , ? )}");
      tryToSetString(1, diseaseName, statement);
      tryToSetString(2, link, statement);
      endWrite(statement);
    }
  }

  /**
   * Outputs all of the results of a table communicated through the Scanner to the console.
   * Otherwise returns an error message.
   *
   * @param scan User input Scanner
   * @param con Connection to diseaseGenomics DB
   */
  private void showAll(Scanner scan, Connection con) {
    ResultSet rs = null;
    CallableStatement statement = null;
    String table = scan.next();
    statement = tryPrepareCall(con, "{call see_table( ? )}");
    tryToSetString(1, table, statement);
    try {
      rs = statement.executeQuery();
    } catch (SQLException e) {
      System.out.println("The table you're trying to view doesnt exist. Try running view with "
          + "another table");
    }
    if (rs != null) {
      showResult(rs);
    }
  }

  /**
   * Allows user to perform precreated queries on the organisms, genes and proteins tables depending
   * on the input taken from the Scanner.
   *
   * @param scan User input Scanner
   * @param con Connection to diseaseGenomics DB
   */
  private void showSubset(Scanner scan, Connection con) {
    ResultSet rs = null;
    while (rs == null) {
      String table = scan.next();
      switch (table) {
        case "organisms":
          rs = handleOrganismCases(scan, con);
          break;
        case "genes":
          rs = handleGeneCases(scan, con);
          break;
        case "proteins":
          rs = handleProtCases(scan, con);
          break;
        default:
          System.out.println("The table you've named isnt subSet query compatible. Try the "
              + "viewSubset with one of the supported tables");
          return;
      }
    }
    if (rs != null) {
      showResult(rs);
    }
    return;
  }

  /**
   * executes stored procedures for the proteins table based on user input.
   *
   * @param s User input Scanner
   * @param c Connection to diseaseGenomics DB
   * @return data resulting from proteins table query
   */
  private ResultSet handleProtCases(Scanner s, Connection c) {
    CallableStatement statement;
    ArrayList<String> organisms = getExistingValues(c, "common_name", "organisms");
    System.out.println("Two options:\n");
    System.out.println("To return the number of aminoacids stored for a given organism type: "
        + "'BP' to receive options");
    System.out.println("To receive information about genes related to a particular organism type:"
        + "'organism_genes' to receive options");
    String choice = s.next();
    if (choice.equals("BP")) {
      System.out.println("options:");
      System.out.println(organisms);
      String chosen_organism = s.next();
      if (organisms.contains(chosen_organism)) {
        statement = tryPrepareCall(c, "{call BP_stored( ? )}");
        tryToSetString(1, chosen_organism, statement);
        return tryToExecuteQuery(statement);
      } else {
        System.out.println("the specified organism doesnt exist here, try again");
        return null;
      }
    } else if (choice.equals("organism_genes")) {
      System.out.println("options:");
      System.out.println(organisms);
      String chosen_organism = s.next();
      if (organisms.contains(chosen_organism)) {
        statement = tryPrepareCall(c, "{call organism_genes( ? )}");
        tryToSetString(1, chosen_organism, statement);
        return tryToExecuteQuery(statement);
      } else {
        System.out.println("the specified organism doesnt exist here, try again");
        return null;
      }
    }
    System.out.println("Please try again using one of the two given options");
    return null;
  }


  /**
   * executes stored procudures for the organisms table based on user input.
   *
   * @param s User input Scanner
   * @param c Connection to diseaseGenomics DB
   * @return data resulting from organisms table query
   */
  private ResultSet handleOrganismCases(Scanner s, Connection c) {
    CallableStatement statement;
    System.out.println("Two options:\n");
    System.out.println("To return only the organisms with a haploid chromosome count over "
        + "a given number - write 'chromosome' followed by your number of choice");
    System.out.println("To return only the organisms with an average lifespan (in days)"
        + "over a certain number - write 'days' followed by your number of choice");
    String choice = s.next();
    if (choice.equals("chromosome")) {
      statement = tryPrepareCall(c, "{call chromosome_over( ? )}");
      tryToSetString(1, s.next(), statement);
      return tryToExecuteQuery(statement);
    } else if (choice.equals("days")) {
      statement = tryPrepareCall(c, "{call days_under( ? )}");
      tryToSetString(1, s.next(), statement);
      return tryToExecuteQuery(statement);
    } else {
      System.out.println("the provided command doesnt exist, try again!");
      return null;
    }
  }

  /**
   * executes stored procudures for the genes table based on user input.
   *
   * @param s User input Scanner
   * @param c Connection to diseaseGenomics DB
   * @return data resulting from genes table query
   */
  private ResultSet handleGeneCases(Scanner s, Connection c) {
    CallableStatement statement;
    System.out.println("To return the name and size of the largest gene (in base pairs) "
        + "currently stored write 'max'");
    System.out.println("To return the name and size of the smallest gene (in base pairs)"
        + "currently stored write 'min'");
    String choice = s.next();
    if (choice.equals("max")) {
      statement = tryPrepareCall(c, "{call max_gene()}");
      return tryToExecuteQuery(statement);
    } else if (choice.equals("min")) {
      statement = tryPrepareCall(c, "{call min_gene()}");
      return tryToExecuteQuery(statement);
    }
    throw new IllegalArgumentException("the inputted strings were not valid");
  }

  /**
   * prints the given result set to the System.out so it can be viewed by the user.
   *
   * @param rs result set to be printed to console.
   */
  private void showResult(ResultSet rs) {
    ResultSetMetaData rsmd = null;
    try {
      rsmd = rs.getMetaData();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    int columnsNumber = 0;
    try {
      columnsNumber = rsmd.getColumnCount();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    while (true) {
      try {
        if (!rs.next()) {
          break;
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
      for (int i = 1; i <= columnsNumber; i++) {
        if (i > 1) {
          System.out.print(",  ");
        }
        String columnValue = null;
        try {
          columnValue = rs.getString(i);
        } catch (SQLException e) {
          e.printStackTrace();
        }
        try {
          System.out.print(capitalize(rsmd.getColumnName(i)) + ": " + columnValue);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
      System.out.println("");
    }
  }

  /**
   * Prints customary confirmation of a correct write to the console and also closes the given
   * statement.
   */
  private void endWrite(CallableStatement statement) {
    try {
      statement.executeQuery();
    } catch (SQLException s) {
      System.out.println("The field types provided were not valid, try again!");
    }
    System.out.println("Write successful!");
    tryToClose(statement);
  }

  /**
   * Prints the available commands to the console for the user to see.
   */
  private void showCommands() {
    System.out.println("This database contains the following view and write functionality:");
    System.out.println("**To write** use the 'writeto' keyword followed by one of the tables"
        + " that can be written to. You will then be prompted for input");
    System.out.println("**To read all values of a table ** use the 'view' "
        + "keyword followed by anyone of the table names below");
    System.out.println("**To read subsets of data ** use the 'viewsubset' keyword followed by one "
        + "of tables labelled as compatible below to receive further instruction");
    System.out.println("**To delete a row of data** use the 'delete' keyword followed by one "
        + "of the tables labelled with (rM)");
    System.out.println("**To modify a row of data ** use the 'modify' keyword followed by one "
        + "of the tables labelled with (rM)\n");
    System.out.println("rM = row modifiable (can call 'delete' and 'modify')");
    System.out.println("w = writeto compatible");
    System.out.println("vA = viewAll compatible");
    System.out.println("vS = viewSubset compatible (Specialized queries available)\n");
    System.out.println("organisms - w, vA, vS");
    System.out.println("genes - w, vA, vS");
    System.out.println("diseases - w, vA");
    System.out.println("proteins - vA, vS, rM");
    System.out.println("organism_diseases - vA, rM");
    System.out.println("organism_organs - vA, rM");
    System.out.println("organs - vA");
    System.out.println("disease_causing_genes - vA\n");
    System.out.println("To leave the database simply write 'q' or 'quit' (non case-sensitive)");
  }

  /**
   * Returns an arrayList<String> of the fields specified in the diseaseGenomics DB with the given
   * table and the given field.
   *
   * @param con Connection to diseaseGenomics DB.
   * @param field field of the given table to be returned in the arrayList.
   * @param table table from which the given field will be grabbed from.
   */
  private ArrayList<String> getExistingValues(Connection con, String field, String table) {
    Statement stmt = null;
    CallableStatement myStmt = tryPrepareCall(con, "{call see_table_field( ? , ? )}");
    tryToSetString(1, field, myStmt);
    tryToSetString(2, table, myStmt);
    ResultSet rs = tryToExecuteQuery(myStmt);
    ArrayList<String> result = new ArrayList<>();
    try {
      while (rs.next()) {
        result.add(rs.getString(field));
      }
    } catch (SQLException e) {
      System.out.println("The given table or field doesn't exist.");
      e.printStackTrace();
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  private ArrayList<String> getExistingOrgans(Connection con) {
    return getExistingValues(con, "organName", "organs");
  }

  private ArrayList<String> getExistingOrganisms(Connection con) {
    return getExistingValues(con, "common_name", "organisms");
  }

  private ArrayList<String> getExistingDiseases(Connection con) {
    return getExistingValues(con, "name", "diseases");
  }

  private ArrayList<String> getExistingProteins(Connection con) {
    return getExistingValues(con, "protein_id", "proteins");
  }

  private ArrayList<String> getExistingGenes(Connection con) {
    return getExistingValues(con, "gene_id", "genes");
  }

  /**
   * Attempts to prepare the given call with the given connection.
   *
   * @param c Connection to be prepared to.
   * @param call Call to be prepared to the given Connection c.
   * @return callable statement.
   * @throws IllegalStateException if call prep fails fails.
   */
  private CallableStatement tryPrepareCall(Connection c, String call) {
    try {
      return c.prepareCall(call);
    } catch (SQLException s) {
      s.getMessage();
    }
    throw new IllegalStateException("failed");
  }

  /**
   * Attempts to set the given String to the value equal to the given number within the given
   * callable statement.
   *
   * @param i index of value to be written
   * @param field field to be written to CallableStatment
   * @param cb CallableStatement to be written to.
   */
  private void tryToSetString(int i, String field, CallableStatement cb) {
    try {
      cb.setString(i, field);
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Attempts to execute the Query of the given statement.
   *
   * @param statement statement to be executed
   * @return result set resulting from statement query execution.
   */
  private ResultSet tryToExecuteQuery(CallableStatement statement) {
    try {
      return statement.executeQuery();
    } catch (SQLException s) {
      s.printStackTrace();
    }
    throw new IllegalStateException("failed");
  }

  /**
   * Attempts to close the given CallableStatement
   *
   * @param statement to be closed.
   */
  private void tryToClose(CallableStatement statement) {
    try {
      statement.close();
    } catch (SQLException x) {
      System.out.println(x.getMessage());
    }
  }

  /**
   * Attempts to close the given Connection.
   *
   * @param con to be closed.
   */
  private void tryToCloseConnection(Connection con) {
    try {
      con.close();
    } catch (SQLException x) {
      System.out.println(x.getMessage());
    }
  }

  private String capitalize(String s) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      if (i == 0) {
        result.append(Character.toUpperCase(s.charAt(i)));
      } else {
        result.append(Character.toLowerCase(s.charAt(i)));
      }
    }
    return result.toString();
  }
}
