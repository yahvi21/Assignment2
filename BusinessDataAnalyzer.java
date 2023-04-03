package com.usf.245.a2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Scanner;

/**
 *
 * @author Yahvi Bhatnagar
 */
public class BusinessDataAnalyzer {

    private static String filePath;
    private static String listType;
    
    private static List<List<Business>> list;
    private static Queue<String> commands;

    // column index of zipcode and naics code in csv file
    private static final int ZIP = 14;
    private static final int NAI = 16;
    private static final int STR = 8;
    private static final int END = 9;
    private static final int NBR = 31;

     /** Check for valid command line arguments.
      * @param String[]args to read comand line arguement
      * @return void
      */
     public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid arguments");
            System.exit(0);
        }
        listType = args[1];
        if (listType.equals("AL")) {
            list = new ArrayList<>();
        } else if (listType.equals("LL")) {
            list = new LinkedList<>();
        } else {
            System.out.println("Invalid arguments");
            System.exit(0);
        }
        filePath = args[0];

        loadData();
		
	// Set up command queue and input scanner for user interaction.
        commands = new ArrayDeque<>();
        Scanner scanner = new Scanner(System.in);
        while (true) {
	    // Execute appropriate action based on user command.
            System.out.print("Command: ");
            String command_input = scanner.nextLine();
            String command = command_input.toLowerCase();
            if (command.equals("summary")) {
                printSummary();
                commands.add(command_input);
            } else if (command.equals("history")) {
                printHistory();
                commands.add(command_input);
            } else if (command.equals("quit")) {
                System.exit(0);
            } else { 
                String[] arr = command.split(" ");
                if (arr.length != 3 || !arr[2].equals("summary")) {
                    System.out.println("Invalid command");
                    continue;
                }
                int code;
                try {
                    code = Integer.parseInt(arr[1]);
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid command");
                    continue;
                }
                if (arr[0].equals("zip")) {
                    printZipSummary(code);
                    commands.add(command_input);
                } else if (arr[0].equals("naics")) {
                    printNaicsSummary(code);
                    commands.add(command_input);
                } else {
                    System.out.println("Invalid command");
                }
            }
        }
    }

    private static void loadData() {
        File file = new File(filePath);
        try {
	    // create a scanner object to read from the file
            Scanner scanner = new Scanner(file);
            // ignore header line in csv file
            scanner.nextLine();
            while (scanner.hasNextLine()) {
		// read the next line from the file and remove all quoted substrings that are not at the beginning or end of a field
                String line = scanner.nextLine();
                line = line.replaceAll("\\B\".*\"\\B", "");
		// split the line into an array of fields
                String[] arr = line.split(",");
                String nbr = "";
                if (arr.length > NBR) {
                    nbr = arr[NBR];
                }
                String end = "";
                if (arr.length > END) {
                    end = arr[END];
                }
                String zip = "";
                if (arr.length > ZIP) {
                    zip = arr[ZIP];
                }
                String nai = "";
                if (arr.length > NAI) {
                    nai = arr[NAI];
                }
                String str = "";
                if (arr.length > STR) {
                    str = arr[STR];
                }
                Business new_business = new Business(zip, nai, str, end, nbr);
		// search through each list of businesses to see if a matching NAICS code is already present
                boolean found = false;
                for (int i = 0; i < list.size(); ++i) {
                    try {
                        List<Business> business_list = list.get(i);
                        Business business = business_list.get(0);
			// if a matching NAICS code is found, the business  adds to the existing list
                        if (business.getNaicsCode().equals(new_business.getNaicsCode())) {
                            business_list.add(new_business);
                            found = true;
                            break;
                        }
                    } catch (Exception ex) {}
                }
		// if no matching NAICS code was found, create a new list of it
                if (!found) {
                    List<Business> business_list;
                    if (listType.equals("AL")) {
                        business_list = new ArrayList<>();
                    } else {
                        business_list = new LinkedList<>();
                    }
                    business_list.add(new_business);   
                    list.add(business_list);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Invalid file");
            System.exit(0);
        }
    }
	
    /** Print the zip summary
     * @param code the zip code to print the summary for
     * @return void
     */
    private static void printZipSummary(int code) {
        int total = 0;
        ArrayList<String> types = new ArrayList<>();
        ArrayList<String> neighborhoods = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            try {
                List<Business> business_list = list.get(i);
                for (int j = 0; j < business_list.size(); ++j) {
                    Business business = business_list.get(j);
		    // If the business is in the specified zip code, increment total count and check if it belongs to a new business type or neighborhood.
                    if (business.getZipCode().equals(code + "")) {
                        ++total;
                        boolean found = false;
                        for (int k = 0; k < types.size(); ++k) {
                            if (business.getNaicsCode().equals(types.get(k))) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            types.add(business.getNaicsCode());
                        }
                        found = false;
                        for (int k = 0; k < neighborhoods.size(); ++k) {
                            if (!business.getNeighborhoods().equals("")) {
                                if (business.getNeighborhoods().equals(neighborhoods.get(k))) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            neighborhoods.add(business.getNeighborhoods());
                        }
                    }
                }
            } catch (Exception ex) {}
        }
	// Print the summary of businesses for the given zip code
        System.out.println("Total Businesses: " + total);
        System.out.println("Business Types: " + types.size());
        System.out.println("Neighborhood: " + neighborhoods.size());
    }
	
     /** Prints a summary of businesses that fall under the specified NAICS code.
      *This method loops through all businesses in the list and counts the ones with the given NAICS code.
      *It also keeps track of unique zip codes and neighborhoods associated with those businesses.
      * @param NAICS code 
      * @return void
      */
      private static void printNaicsSummary(int code) {
        int total = 0;
        ArrayList<String> codes = new ArrayList<>();
        ArrayList<String> neighborhoods = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            try {
                List<Business> business_list = list.get(i);
                for (int j = 0; j < business_list.size(); ++j) {
                    Business business = business_list.get(j);
					// Get the NAICS code of this business and split it into start and end numbers
                    String naics = business.getNaicsCode();
                    String[] arr = naics.split("-");
                    int start = Integer.parseInt(arr[0]);
                    int end = Integer.parseInt(arr[1]);
            
                    if (code >= start && code <= end) {
                        ++total;
                        boolean found = false;
						// Check if this zip code has already been added
                        for (int k = 0; k < codes.size(); ++k) {
                            if (business.getZipCode().equals(codes.get(k))) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            codes.add(business.getZipCode());
                        }
						// Check if this neighborhood has already been added
                        found = false;
                        for (int k = 0; k < neighborhoods.size(); ++k) {
                            if (!business.getNeighborhoods().equals("")) {
                                if (business.getNeighborhoods().equals(neighborhoods.get(k))) {
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            neighborhoods.add(business.getNeighborhoods());
                        }
                    }
                }
            } catch (Exception ex) {}
        }
        System.out.println("Total Businesses: " + total);
        System.out.println("Zip codes: " + codes.size());
        System.out.println("Neighborhood: " + neighborhoods.size());
    }
    
    private static void printSummary() {
        int total = 0, closed = 0, lastYear = 0;
        for (int i = 0; i < list.size(); ++i) {
            try {
		// Get the current list of businesses
                List<Business> business_list = list.get(i);
                for (int j = 0; j < business_list.size(); ++j) {
                    ++total;
                    // If the business is closed (has an end date), increment the closed business count
		    Business business = business_list.get(j);
                    if (!business.getEndDate().equals("")) {
                        ++closed;
                    } 
                    String start = business.getStartDate();
                    String[] arr = start.split("[/-]");
                    String year = "";
                    if (arr.length == 3) {
                        year = arr[2];
                    }
		    //For new business in last years checks for year 2022 or 2023
                    if (year.equals(2022) || (year.equals(2023))) {
                        ++lastYear;
                    }
                }
            } catch (Exception ex) {}
        }
        System.out.println("Total Businesses: " + total);
        System.out.println("Closed Businesses: " + closed);
        System.out.println("New Business in last year: " + lastYear);
    }
	
    // Loop through each string in the commands list and print it out
    private static void printHistory() {
        for (String command: commands) {
            System.out.println(command);
        }
    }
}

class Business {
    private String zipCode;
    private String naicsCode;
    private String startDate;
    private String endDate;
    private String neighborhoods;

    public Business(String zipCode, String naicsCode, String startDate, String endDate, String neighborhoods) {
        this.zipCode = zipCode;
        this.naicsCode = naicsCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.neighborhoods = neighborhoods;
    }

    public String getZipCode() {
        return zipCode;
    }
    
    public String getNaicsCode() {
        return naicsCode;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getNeighborhoods() {
        return neighborhoods;
    }
}
//Implement iterator interface
interface List<T> {
    T get(int pos) throws Exception;
    boolean add(T item);
    void add(int pos, T item);
    T remove(int pos);
    int size();
    Iterator iterator();
}

interface Iterator<T> {
    boolean hasNext();
    T next();
}

class ArrayList<T> implements List<T> {
    private T[] arr;
    private int size;

    public ArrayList() {
	// Will generate a warning
        arr = (T[]) new Object[10]; 
        size = 0;
    }
    //Returns size of the list 
    public int size() {
        return size;
    }
    //Returns element at the specified postion in the list
    public T get(int pos) throws Exception {
        if (pos < 0 || pos >= size) {
			//Exception is thrown if position is invalid
            throw new Exception("Invalid position ");
        }
        return arr[pos];
    }
    // Method to double the size of the array when it is full
    private void grow_array() {
        T[] new_arr = (T[]) new Object[arr.length * 2];
        for (int i = 0; i < arr.length; i++) {
            new_arr[i] = arr[i];
        }
        arr = new_arr;
    }
    // Method to add an element to the end of the list
    public boolean add(T item) {
        if (size == arr.length) {
            grow_array();
        }
        arr[size++] = item;
        return true;
    }
    //Adds a specified element to the specifed position in the list 
    public void add(int pos, T item) {
        if (size == arr.length) {
            grow_array();
        }
        for (int i = size; i > pos; i--) {
            arr[i] = arr[i - 1];
        }
        arr[pos] = item;
        ++size;
    }

    //Removes the specified element and shifts the list to left by 1
    public T remove(int pos) {
        T item = arr[pos];
        for (int i = pos; i < size - 1; i++) {
            arr[i] = arr[i + 1];
        }
        --size;
        return item;
    }
	//Implement intterator interface
 	private class ListIterator<T> implements Iterator<T> {
        private int nextIndex = 0;

        public boolean hasNext() {
            return nextIndex < size && nextIndex >= 0;
        }

        public T next() {
            return (T) arr[nextIndex++];
        }
    }

    public Iterator iterator() {
        return new ListIterator();
    }
}

/* The LinkedList class implements the List interface and provides a basic 
 * implementation of a singly linked list data structure.
 * @param <T> the type of elements stored in the list
 */
class LinkedList<T> implements List<T> {
    private Node<T> head;
    private int size;
    
    private class Node<T> {
        private T data;
        private Node<T> next;

        public Node(T value) {
            data = value;
            next = null;
        }
    }

    public LinkedList() {
        head = null;
        size = 0;
    }

    public int size() {
        return size;
    }
	
    //Returns the element at the specified position in the list 
    public T get(int pos) {
        Node curr = head;
        for (int i = 0; i < pos; i++) {
            curr = curr.next;
        }
        return (T) curr.data;
    }
	
    //Adds the specified element to the end of the list
    public boolean add(T item) {
        if (head == null) {
            head = new Node(item);
            ++size;
            return true;
        }
        Node prev = head;
        for (int i = 0; i < size;
                i++) {
            prev = prev.next;
        }
        Node node = new Node(item);
        prev.next = node;
        ++size;
        return true;
    }
	
    //Inserts the specified element at the specified position in the list
    public void add(int pos, T item) {
        if (pos == 0) {
            Node node = new Node(item);
            node.next = head;
            head = node;
            ++size;
        } else {
            Node prev = head;
            for (int i = 0; i < pos - 1;
                    i++) {
                prev = prev.next;
            }
            Node node = new Node(item);
            node.next = prev.next;
            prev.next = node;
            ++size;
        }
    }
	
    //Removes element at the specific position
    public T remove(int pos) {
        if (pos == 0) {
            Node node = head;
            head = head.next;
            --size;
            return (T) node.data;
        } else {
            Node prev = head;
            for (int i = 0; i < pos - 1; i++) {
                prev = prev.next;
            }
            Node node = prev.next;
            prev.next = node.next;
            --size;
            return (T) node.data;
        }
    }
	
    //Iterator for the linked list
    private class ListIterator<T> implements Iterator<T> {
        private Node node = head;

        public boolean hasNext() {
            return node.next != null;
        }

        public T next() {
            Node prev = node;
            node = node.next;
            return (T) prev.data;
        }
    }
    
    public Iterator iterator() {
        return new ListIterator();
    }
}
