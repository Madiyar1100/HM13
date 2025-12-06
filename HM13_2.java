import java.util.*;
import java.io.*;
import java.util.concurrent.*;

interface State {
    void selectTicket(int price);
    void insertMoney(int amount);
    void cancel();
    void dispense();
    String name();
}

class TicketVendingMachine {
    State idleState;
    State waitingState;
    State moneyReceivedState;
    State ticketDispensedState;
    State transactionCanceledState;
    State current;
    int price;
    int balance;
    TicketVendingMachine(){
        idleState = new IdleState(this);
        waitingState = new WaitingForMoneyState(this);
        moneyReceivedState = new MoneyReceivedState(this);
        ticketDispensedState = new TicketDispensedState(this);
        transactionCanceledState = new TransactionCanceledState(this);
        current = idleState;
        price = 0;
        balance = 0;
    }
    void setState(State s){ current = s; }
    State getIdle(){ return idleState; }
    State getWaiting(){ return waitingState; }
    State getMoneyReceived(){ return moneyReceivedState; }
    State getTicketDispensed(){ return ticketDispensedState; }
    State getTransactionCanceled(){ return transactionCanceledState; }
    void selectTicket(int p){ current.selectTicket(p); }
    void insertMoney(int amount){ current.insertMoney(amount); }
    void cancel(){ current.cancel(); }
    void dispense(){ current.dispense(); }
    void setPrice(int p){ price = p; }
    int getPrice(){ return price; }
    void addBalance(int a){ balance += a; }
    int getBalance(){ return balance; }
    void resetBalance(){ balance = 0; }
}

class IdleState implements State {
    TicketVendingMachine m;
    IdleState(TicketVendingMachine m){ this.m = m; }
    public void selectTicket(int price){
        m.setPrice(price);
        m.setState(m.getWaiting());
        System.out.println("Билет выбран. Цена: "+price);
    }
    public void insertMoney(int amount){ System.out.println("Выберите билет сначала."); }
    public void cancel(){ System.out.println("Нет активной транзакции."); }
    public void dispense(){ System.out.println("Нечего выдавать."); }
    public String name(){ return "Idle"; }
}

class WaitingForMoneyState implements State {
    TicketVendingMachine m;
    WaitingForMoneyState(TicketVendingMachine m){ this.m = m; }
    public void selectTicket(int price){ System.out.println("Билет уже выбран."); }
    public void insertMoney(int amount){
        m.addBalance(amount);
        System.out.println("Внесено: "+m.getBalance()+"/"+m.getPrice());
        if(m.getBalance() >= m.getPrice()){
            m.setState(m.getMoneyReceived());
            System.out.println("Достаточно средств.");
        }
    }
    public void cancel(){
        m.setState(m.getTransactionCanceled());
        m.getTransactionCanceled().cancel();
    }
    public void dispense(){ System.out.println("Недостаточно средств."); }
    public String name(){ return "WaitingForMoney"; }
}

class MoneyReceivedState implements State {
    TicketVendingMachine m;
    MoneyReceivedState(TicketVendingMachine m){ this.m = m; }
    public void selectTicket(int price){ System.out.println("Транзакция в процессе."); }
    public void insertMoney(int amount){
        m.addBalance(amount);
        System.out.println("Внесено: "+m.getBalance()+"/"+m.getPrice());
    }
    public void cancel(){
        m.setState(m.getTransactionCanceled());
        m.getTransactionCanceled().cancel();
    }
    public void dispense(){
        m.setState(m.getTicketDispensed());
        m.getTicketDispensed().dispense();
    }
    public String name(){ return "MoneyReceived"; }
}

class TicketDispensedState implements State {
    TicketVendingMachine m;
    TicketDispensedState(TicketVendingMachine m){ this.m = m; }
    public void selectTicket(int price){ System.out.println("Подождите, выдача..."); }
    public void insertMoney(int amount){ System.out.println("Подождите, выдача..."); }
    public void cancel(){ System.out.println("Нельзя отменить, билет будет выдан."); }
    public void dispense(){
        int change = m.getBalance() - m.getPrice();
        System.out.println("Билет выдан. Сдача: "+change);
        m.resetBalance();
        m.setPrice(0);
        m.setState(m.getIdle());
    }
    public String name(){ return "TicketDispensed"; }
}

class TransactionCanceledState implements State {
    TicketVendingMachine m;
    TransactionCanceledState(TicketVendingMachine m){ this.m = m; }
    public void selectTicket(int price){ System.out.println("Транзакция отменяется, подождите."); }
    public void insertMoney(int amount){ System.out.println("Транзакция отменяется, возврат средств..."); }
    public void cancel(){
        int refund = m.getBalance();
        System.out.println("Транзакция отменена. Возврат: "+refund);
        m.resetBalance();
        m.setPrice(0);
        m.setState(m.getIdle());
    }
    public void dispense(){ System.out.println("Транзакция отменена."); }
    public String name(){ return "TransactionCanceled"; }
}

public class TicketVendingMachineApp {
    public static void main(String[] args) throws Exception{
        TicketVendingMachine machine = new TicketVendingMachine();
        Scanner sc = new Scanner(System.in, "UTF-8");
        while(true){
            System.out.println("\nТекущее состояние: "+machine.current.name());
            System.out.println("1 Выбрать билет\n2 Внести деньги\n3 Отмена\n4 Выдать билет (оператор)\n0 Выход");
            String cmd = sc.nextLine().trim();
            switch(cmd){
                case "1":
                    System.out.print("Введите цену билета (целое): ");
                    try { int p = Integer.parseInt(sc.nextLine().trim()); machine.selectTicket(p); } catch(Exception e){ System.out.println("Неверная цена"); }
                    break;
                case "2":
                    System.out.print("Введите сумму (целое): ");
                    try { int a = Integer.parseInt(sc.nextLine().trim()); machine.insertMoney(a); } catch(Exception e){ System.out.println("Неверная сумма"); }
                    break;
                case "3":
                    machine.cancel();
                    break;
                case "4":
                    machine.dispense();
                    break;
                case "0":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Неверная команда");
            }
        }
    }
}
