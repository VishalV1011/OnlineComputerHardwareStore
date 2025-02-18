import java.sql.*;
import java.util.Scanner;

public class BasicJDBCDemo {
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		BasicJDBC db = new BasicJDBC();

		int menu;
		char proceed = 'y';
		String name, email;
		int id, age;


		try {
			//Create a connection to the database
			Connection conn = db.getConnection();
			System.out.println("Database successfully connected!\n");
			//Create a Statement object
			Statement stmt = conn.createStatement();
		do {
			System.out.println("1. Search");
			System.out.println("2. Update age");
			System.out.println("3. Insert");
			System.out.println("4. Delete");
			System.out.println("Please select menu (1/2/3/4): ");
			menu = input.nextInt();
			input.nextLine();

			if (menu==1) {
				System.out.println("Enter ID: ");
				id = input.nextInt();
				db.search(stmt, id);
			}

			else if (menu==2) {
				db.viewAll(stmt);
				System.out.println("Enter ID to update his age: ");
				id = input.nextInt();

				if(db.findAndDisplay(stmt, id)) {
					System.out.println("Enter new age: ");
					age = input.nextInt();
					input.nextLine();
					db.updateAge(stmt, id, age);
				}

				else {
					System.out.println("The id was not found");
				}
			}

			else if (menu==3) {
				//Get the new data for the new person
				System.out.println("Enter the name: ");
				name = input.nextLine();

				System.out.println("Enter the age: ");
				age = id = input.nextInt();
				input.nextLine();
				System.out.println("Enter the email: ");
				email = input.nextLine();

				db.insert(stmt, name, email, age);
			}

			else if (menu==4) {
				db.viewAll(stmt);
				System.out.println("Enter ID to delete: ");
				id = input.nextInt();
				input.nextLine();

				if(db.findAndDisplay(stmt, id)) {
					db.delete(stmt, id);
				}

				else {
					System.out.println("The id was not found");
				}
			}

			else {
				System.out.println("Sorry you key in wrong menu!");
			}

			System.out.println("\nDo you want to continue (y for yes, n for no): ");
			proceed = input.next().charAt(0);
			}while (proceed=='y' || proceed=='Y');
			
			stmt.close();
			conn.close();
			System.out.println("\nThank you.");
			}
			catch (Exception e) {
			e.printStackTrace();
		}
	}
}