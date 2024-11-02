package htw.berlin.prog2.ha1;

import java.util.Objects;

/**
 * Eine Klasse, die das Verhalten des Online Taschenrechners imitiert, welcher auf
 * https://www.online-calculator.com/ aufgerufen werden kann (ohne die Memory-Funktionen)
 * und dessen Bildschirm bis zu zehn Ziffern plus einem Dezimaltrennzeichen darstellen kann.
 * Enthält mit Absicht noch diverse Bugs oder unvollständige Funktionen.
 */
public class Calculator {

    private String screen = "0";

    private double latestValue;

    private String latestOperation = "";

    private boolean isNewOperation = false;


    /**
     * @return den aktuellen Bildschirminhalt als String
     */
    public String readScreen() {
        return screen;
    }

    /**
     * Empfängt den Wert einer gedrückten Zifferntaste. Da man nur eine Taste auf einmal
     * drücken kann muss der Wert positiv und einstellig sein und zwischen 0 und 9 liegen.
     * Führt in jedem Fall dazu, dass die gerade gedrückte Ziffer auf dem Bildschirm angezeigt
     * oder rechts an die zuvor gedrückte Ziffer angehängt angezeigt wird.
     * @param digit Die Ziffer, deren Taste gedrückt wurde
     */
    public void pressDigitKey(int digit) {
        if (digit < 0 || digit > 9) throw new IllegalArgumentException();

        if (screen.equals("0") || isNewOperation && screen.equals("")) {
            screen = String.valueOf(digit);

            //isNewOperation = false;
        } else {
            screen += digit;
        }
    }

    /**
     * Empfängt den Befehl der C- bzw. CE-Taste (Clear bzw. Clear Entry).
     * Einmaliges Drücken der Taste löscht die zuvor eingegebenen Ziffern auf dem Bildschirm
     * so dass "0" angezeigt wird, jedoch ohne zuvor zwischengespeicherte Werte zu löschen.
     * Wird daraufhin noch einmal die Taste gedrückt, dann werden auch zwischengespeicherte
     * Werte sowie der aktuelle Operationsmodus zurückgesetzt, so dass der Rechner wieder
     * im Ursprungszustand ist.
     */
    public void pressClearKey() {
        screen = "0";
        latestOperation = "";
        latestValue = 0.0;
    }

    /**
     * Empfängt den Wert einer gedrückten binären Operationstasten, also eine der vier Operationen
     * Addition, Substraktion, Division, oder Multiplikation, welche zwei Operanden benötigen.
     * Beim ersten Drücken der Taste wird der Bildschirminhalt nicht verändert, sondern nur der
     * Rechner in den passenden Operationsmodus versetzt.
     * Beim zweiten Drücken nach Eingabe einer weiteren Zahl wird direkt das aktuelle Zwischenergebnis
     * über die Hilsfunktion calculateResult berechnet und auf dem Bildschirm angezeigt.
     * @param operation "+" für Addition, "-" für Substraktion, "x" für Multiplikation, "/" für Division
     */

    public void pressBinaryOperationKey(String operation) {
        if (!screen.isEmpty()) {
            if (isNewOperation == true && !operation.equals("")) {
                calculateResult();

            }else {
                latestValue = Double.parseDouble(screen);

            }
        }
        latestOperation = operation;
        isNewOperation = true;
        screen = "";
    }


    /**
     * Empfängt den Wert einer gedrückten unären Operationstaste, also eine der drei Operationen
     * Quadratwurzel, Prozent, Inversion, welche nur einen Operanden benötigen.
     * Beim Drücken der Taste wird direkt die Operation auf den aktuellen Zahlenwert angewendet und
     * der Bildschirminhalt mit dem Ergebnis aktualisiert.
     * Falls hierbei eine Division durch Null auftritt, wird "Error" angezeigt.
     * Außerdem werden Unendlichkeit & NaN als "Error" ausgegeben.
     * @param operation "√" für Quadratwurzel, "%" für Prozent, "1/x" für Inversion
     */
    public void pressUnaryOperationKey(String operation) {

        latestValue = Double.parseDouble(screen);
        latestOperation = operation;
        var result = switch(operation) {
            case "√" -> Math.sqrt(Double.parseDouble(screen));
            case "%" -> Double.parseDouble(screen) / 100;
            case "1/x" -> 1 / Double.parseDouble(screen);
            default -> throw new IllegalArgumentException();
        };
        screen = Double.toString(result);

        if(screen.equals("NaN")) screen = "Error";
        if(screen.contains(".") && screen.length() > 11) screen = screen.substring(0, 10);
        if(screen.equals("Infinity")) screen = "Error";

    }


    /**
     * Empfängt den Befehl der gedrückten Dezimaltrennzeichentaste, im Englischen üblicherweise "."
     * Fügt beim ersten Mal Drücken dem aktuellen Bildschirminhalt das Trennzeichen auf der rechten
     * Seite hinzu und aktualisiert den Bildschirm. Daraufhin eingegebene Zahlen werden rechts vom
     * Trennzeichen angegeben und daher als Dezimalziffern interpretiert.
     * Beim zweimaligem Drücken, oder wenn bereits ein Trennzeichen angezeigt wird, passiert nichts.
     */
    public void pressDotKey() {
        if(!screen.contains(".")) screen = screen + ".";
    }

    /**
     * Empfängt den Befehl der gedrückten Vorzeichenumkehrstaste ("+/-").
     * Zeigt der Bildschirm einen positiven Wert an, so wird ein "-" links angehängt, der Bildschirm
     * aktualisiert und die Inhalt fortan als negativ interpretiert.
     * Zeigt der Bildschirm bereits einen negativen Wert mit führendem Minus an, dann wird dieses
     * entfernt und der Inhalt fortan als positiv interpretiert.
     */
    public void pressNegativeKey() {
        screen = screen.startsWith("-") ? screen.substring(1) : "-" + screen;
    }

    /**
     * Empfängt den Befehl der gedrückten "="-Taste.
     * Wurde zuvor keine Operationstaste gedrückt, passiert nichts.
     * Wurde zuvor eine binäre Operationstaste gedrückt und zwei Operanden eingegeben, wird das
     * Ergebnis der Operation über die Hilfsmethode calculateResult berechnet.
     * Wird die Taste weitere Male gedrückt (ohne andere Tasten dazwischen), so wird die letzte
     * Operation (ggf. inklusive letztem Operand) erneut auf den aktuellen Bildschirminhalt angewandt
     * und das Ergebnis direkt angezeigt.
     */
    public void pressEqualsKey() {
        if (!latestOperation.isEmpty() && !screen.equals("")) {
            calculateResult();
            latestOperation = "";
        }
    }

    /**
     * Öffentliche Hilfsmethode zum Berechnen der binären Operationen.
     * Falls hierbei eine Division durch Null auftritt, wird "Error" angezeigt.
     * Außerdem werden Unendlichkeit & NaN als "Error" ausgegeben.
     * Die Länge des Ergebnis wird auf die Anzahl von 11 Zeichen reduziert um die Darstellbarkeit im "Display" zu gewährleisten.
     * Kommazahlen werden so eingekürzt, dass nur eine Null im Nachkommabereich angezeigt wird.
     * Benutzt die selben Paramter wie die Methode pressBinaryOperationKey.
     * Im Falle einer Zwischenspeicherberechnung wird das Ergebnis zur Weiterberechnung in latestValue gespeichert.
     */
    public void calculateResult() {
        double currentValue = Double.parseDouble(screen);
        double result = switch (latestOperation) {
            case "+" -> latestValue + currentValue;
            case "-" -> latestValue - currentValue;
            case "x" -> latestValue * currentValue;
            case "/" -> currentValue == 0 ? Double.NaN : latestValue / currentValue;
            default -> throw new IllegalArgumentException();
        };
        screen = Double.toString(result);
        if (screen.equals("NaN")) screen = "Error";
        if (screen.equals("Infinity")) screen = "Error";
        if (screen.contains(".") && screen.length() > 11) screen = screen.substring(0, 10);
        if (screen.endsWith(".0")) screen = screen.substring(0, screen.length() - 2);
        latestValue = result;
    }
}






