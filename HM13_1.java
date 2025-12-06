import java.util.*;
import java.time.*;

public class LibrarySystem {
    static abstract class User {
        UUID id;
        String name;
        String role;
        User(String name,String role){this.id=UUID.randomUUID();this.name=name;this.role=role;}
    }
    static class Reader extends User{
        List<Reservation> history=new ArrayList<>();
        Reader(String name){super(name,"Reader");}
    }
    static class Librarian extends Reader{
        Librarian(String name){super(name);this.role="Librarian";}
    }
    static class Administrator extends Librarian{
        Administrator(String name){super(name);this.role="Administrator";}
    }
    static class Book {
        enum State{AVAILABLE,RESERVED,CHECKED_OUT,OVERDUE,RETURNED,LOST}
        UUID id;
        String title;
        String author;
        String genre;
        State state;
        Book(String t,String a,String g){this.id=UUID.randomUUID();this.title=t;this.author=a;this.genre=g;this.state=State.AVAILABLE;}
        public String toString(){return String.format("%s | %s | %s | %s",id.toString().substring(0,8),title,author,state);}
    }
    static class Branch{
        UUID id; String name;
        Map<UUID,Book> books=new HashMap<>();
        Branch(String name){this.id=UUID.randomUUID();this.name=name;}
    }
    static class Reservation{
        UUID id; UUID bookId; UUID userId; LocalDate date; boolean active;
        Reservation(UUID b,UUID u){this.id=UUID.randomUUID();this.bookId=b;this.userId=u;this.date=LocalDate.now();this.active=true;}
        public String toString(){return id.toString().substring(0,8)+" book:"+bookId.toString().substring(0,8)+" user:"+userId.toString().substring(0,8)+" date:"+date;}
    }
    static class Data {
        Map<UUID,User> users=new HashMap<>();
        Map<UUID,Branch> branches=new HashMap<>();
        Map<UUID,Reservation> reservations=new HashMap<>();
        Map<UUID,UUID> checkedOutBy=new HashMap<>();
    }
    static Data data=new Data();
    static Scanner sc=new Scanner(System.in, "UTF-8");
    public static void main(String[] args){
        seedDemo();
        while(true){
            System.out.println("\n1 Регистрация читателя\n2 Добавить книгу (библиотекарь/админ)\n3 Просмотр всех книг\n4 Поиск книг\n5 Бронирование книги\n6 Отмена бронирования\n7 Выдача книги\n8 Возврат книги\n9 Просмотр истории бронирований пользователя\n10 Управление филиалами (админ)\n11 Просмотр аналитики (админ)\n0 Выход");
            String c=sc.nextLine().trim();
            try{
                switch(c){
                    case "1": register(); break;
                    case "2": addBook(); break;
                    case "3": listAllBooks(); break;
                    case "4": searchBooks(); break;
                    case "5": reserveBook(); break;
                    case "6": cancelReservation(); break;
                    case "7": issueBook(); break;
                    case "8": returnBook(); break;
                    case "9": viewUserHistory(); break;
                    case "10": manageBranches(); break;
                    case "11": analytics(); break;
                    case "0": System.exit(0);
                    default: System.out.println("Неверный выбор");
                }
            }catch(Exception e){
                System.out.println("Ошибка: "+e.getMessage());
            }
        }
    }
    static void seedDemo(){
        Administrator admin=new Administrator("Admin");
        data.users.put(admin.id,admin);
        Librarian lib=new Librarian("Ivan");
        data.users.put(lib.id,lib);
        Reader r=new Reader("Petr");
        data.users.put(r.id,r);
        Branch b1=new Branch("Центральная");
        Branch b2=new Branch("Филиал №1");
        data.branches.put(b1.id,b1);
        data.branches.put(b2.id,b2);
        addBookToBranch(b1,"Война и мир","Толстой","Роман");
        addBookToBranch(b1,"Преступление и наказание","Достоевский","Роман");
        addBookToBranch(b2,"Java. Руководство","Шилдт","Техническая");
    }
    static void register(){
        System.out.print("Имя: ");
        String name=sc.nextLine().trim();
        Reader r=new Reader(name);
        data.users.put(r.id,r);
        System.out.println("Зарегистрирован: id="+r.id.toString().substring(0,8));
    }
    static Branch chooseBranch(){
        if(data.branches.isEmpty()){System.out.println("Нет филиалов");return null;}
        List<Branch> list=new ArrayList<>(data.branches.values());
        for(int i=0;i<list.size();i++) System.out.println((i+1)+ " " + list.get(i).name);
        System.out.print("Выберите филиал: ");
        int idx=Integer.parseInt(sc.nextLine().trim())-1;
        if(idx<0||idx>=list.size()) return null;
        return list.get(idx);
    }
    static void addBook(){
        System.out.print("Ваш id (библиотекарь/админ): ");
        String uid=sc.nextLine().trim();
        User u=findUserByShortId(uid);
        if(u==null||!(u instanceof Librarian || u instanceof Administrator)){System.out.println("Нет прав");return;}
        Branch br=chooseBranch();
        if(br==null){System.out.println("Филиал не выбран");return;}
        System.out.print("Название: ");String t=sc.nextLine().trim();
        System.out.print("Автор: ");String a=sc.nextLine().trim();
        System.out.print("Жанр: ");String g=sc.nextLine().trim();
        addBookToBranch(br,t,a,g);
        System.out.println("Книга добавлена");
    }
    static void addBookToBranch(Branch br,String t,String a,String g){
        Book b=new Book(t,a,g);
        br.books.put(b.id,b);
    }
    static void listAllBooks(){
        for(Branch br:data.branches.values()){
            System.out.println("Филиал: "+br.name);
            for(Book b:br.books.values()) System.out.println(b);
        }
    }
    static void searchBooks(){
        System.out.print("Поиск по (title/author/genre): ");
        String f=sc.nextLine().trim().toLowerCase();
        System.out.print("Запрос: ");
        String q=sc.nextLine().trim().toLowerCase();
        for(Branch br:data.branches.values()){
            for(Book b:br.books.values()){
                boolean ok=false;
                if(f.equals("title")&&b.title.toLowerCase().contains(q)) ok=true;
                if(f.equals("author")&&b.author.toLowerCase().contains(q)) ok=true;
                if(f.equals("genre")&&b.genre.toLowerCase().contains(q)) ok=true;
                if(ok) System.out.println(br.name+" | "+b);
            }
        }
    }
    static void reserveBook(){
        System.out.print("Ваш id: ");
        String uid=sc.nextLine().trim();
        User u=findUserByShortId(uid);
        if(u==null){System.out.println("Пользователь не найден");return;}
        System.out.print("id книги (8 символов): ");
        String bid=sc.nextLine().trim();
        Book b=findBookByShortId(bid);
        if(b==null){System.out.println("Книга не найдена");return;}
        if(b.state!=Book.State.AVAILABLE){System.out.println("Книга недоступна");return;}
        Reservation r=new Reservation(b.id,u.id);
        data.reservations.put(r.id,r);
        b.state=Book.State.RESERVED;
        if(u instanceof Reader) ((Reader)u).history.add(r);
        System.out.println("Забронирована. reservation id: "+r.id.toString().substring(0,8));
    }
    static void cancelReservation(){
        System.out.print("Ваш id: ");
        String uid=sc.nextLine().trim();
        User u=findUserByShortId(uid); if(u==null){System.out.println("Нет");return;}
        System.out.print("id резерва (8): ");
        String rid=sc.nextLine().trim();
        Reservation r=findReservationByShortId(rid);
        if(r==null||!r.active){System.out.println("Резерв не найден или неактивен");return;}
        r.active=false;
        Book b=findBookById(r.bookId);
        if(b!=null) b.state=Book.State.AVAILABLE;
        System.out.println("Бронирование отменено");
    }
    static void issueBook(){
        System.out.print("Ваш id (библиотекарь/админ): ");
        String uid=sc.nextLine().trim();
        User u=findUserByShortId(uid);
        if(u==null||!(u instanceof Librarian || u instanceof Administrator)){System.out.println("Нет прав");return;}
        System.out.print("id книги (8): ");
        String bid=sc.nextLine().trim();
        Book b=findBookByShortId(bid); if(b==null){System.out.println("Нет");return;}
        System.out.print("id читателя (8): ");
        String rid=sc.nextLine().trim();
        User user=findUserByShortId(rid); if(user==null){System.out.println("Нет");return;}
        if(b.state==Book.State.CHECKED_OUT){System.out.println("Уже выдана");return;}
        b.state=Book.State.CHECKED_OUT;
        data.checkedOutBy.put(b.id,user.id);
        System.out.println("Книга выдана");
    }
    static void returnBook(){
        System.out.print("id книги (8): ");
        String bid=sc.nextLine().trim();
        Book b=findBookByShortId(bid); if(b==null){System.out.println("Нет");return;}
        b.state=Book.State.RETURNED;
        data.checkedOutBy.remove(b.id);
        b.state=Book.State.AVAILABLE;
        System.out.println("Книга возвращена и доступна");
    }
    static void viewUserHistory(){
        System.out.print("id пользователя (8): ");
        String uid=sc.nextLine().trim();
        User u=findUserByShortId(uid);
        if(u==null||!(u instanceof Reader)){System.out.println("Нет");return;}
        Reader r=(Reader)u;
        for(Reservation res:r.history) System.out.println(res);
    }
    static void manageBranches(){
        System.out.print("Ваш id (админ): ");
        String uid=sc.nextLine().trim();
        User u=findUserByShortId(uid);
        if(u==null||!(u instanceof Administrator)){System.out.println("Нет прав");return;}
        System.out.println("1 Добавить филиал\n2 Удалить филиал");
        String c=sc.nextLine().trim();
        if(c.equals("1")){
            System.out.print("Название: "); String n=sc.nextLine().trim();
            Branch b=new Branch(n); data.branches.put(b.id,b); System.out.println("Добавлен id:"+b.id.toString().substring(0,8));
        } else if(c.equals("2")){
            List<Branch> list=new ArrayList<>(data.branches.values());
            for(int i=0;i<list.size();i++) System.out.println((i+1)+" "+list.get(i).name);
            System.out.print("Выберите: "); int idx=Integer.parseInt(sc.nextLine().trim())-1;
            if(idx<0||idx>=list.size()) {System.out.println("Неверно");return;}
            data.branches.remove(list.get(idx).id); System.out.println("Удалён");
        }
    }
    static void analytics(){
        System.out.print("Ваш id (админ): ");
        String uid=sc.nextLine().trim();
        User u=findUserByShortId(uid);
        if(u==null||!(u instanceof Administrator)){System.out.println("Нет прав");return;}
        int total=0; Map<String,Integer> popularity=new HashMap<>();
        for(Branch br:data.branches.values()){
            for(Book b:br.books.values()){
                total++;
                popularity.put(b.title, popularity.getOrDefault(b.title,0)+ (b.state==Book.State.CHECKED_OUT?1:0));
            }
        }
        System.out.println("Всего книг: "+total);
        List<Map.Entry<String,Integer>> list=new ArrayList<>(popularity.entrySet());
        list.sort((a,b)->b.getValue()-a.getValue());
        System.out.println("Топ выданных книг:");
        for(int i=0;i<Math.min(5,list.size());i++) System.out.println((i+1)+" "+list.get(i).getKey()+" ("+list.get(i).getValue()+")");
    }
    static User findUserByShortId(String s){
        s=s.toLowerCase();
        for(User u:data.users.values()) if(u.id.toString().toLowerCase().startsWith(s)) return u;
        return null;
    }
    static Book findBookByShortId(String s){
        s=s.toLowerCase();
        for(Branch br:data.branches.values()){
            for(Book b:br.books.values()) if(b.id.toString().toLowerCase().startsWith(s)) return b;
        }
        return null;
    }
    static Book findBookById(UUID id){
        for(Branch br:data.branches.values()) if(br.books.containsKey(id)) return br.books.get(id);
        return null;
    }
    static Reservation findReservationByShortId(String s){
        s=s.toLowerCase();
        for(Reservation r:data.reservations.values()) if(r.id.toString().toLowerCase().startsWith(s)) return r;
        return null;
    }
}
