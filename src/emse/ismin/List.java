package emse.ismin;

class List {

    private ListElement head;

    ListElement getHead() {
        return head;
    }

    int length() {
        int length = 0;
        ListElement i = getHead();
        while (i != null) {
            length++;
            i = i.getNext();
        }
        return length;
    }

    void addScore(Score score) {
        if (head == null) {
            head = new ListElement(score);
        }
        else {
            ListElement element = getHead();
            while (element.getNext() != null) {
                element = element.getNext();
            }
            element.setNext(new ListElement(score));
        }
    }
}

class ListElement {

    private Score score;
    private ListElement next;

    ListElement(Score score) {
        this.score = score;
        this.next = null;
    }

    Score getScore() {
        return score;
    }

    ListElement getNext() {
        return next;
    }

    void setNext(ListElement next) {
        this.next = next;
    }
}