package emse.ismin.minesweeper;

/**
 * This class is a basic implementation of a linked list.
 */
class List {

    private ListElement head;

    /**
     * @param score add a score to the linked list.
     */
    void addScore(Score score) {
        if (head == null) {
            head = new ListElement(score);
        } else {
            ListElement element = getHead();
            while (element.getNext() != null) {
                element = element.getNext();
            }
            element.setNext(new ListElement(score));
        }
    }

    /**
     * @return the first element of the list.
     */
    ListElement getHead() {
        return head;
    }
}

/**
 * This class composes each cell of the linked list.
 */
class ListElement {

    private Score score;
    private ListElement next;

    /**
     * @param score is the new score that needs to be added.
     */
    ListElement(Score score) {
        this.score = score;
        this.next = null;
    }

    /**
     * @return returns the score of this list's cell.
     */
    Score getScore() {
        return score;
    }

    /**
     * @return returns the following element in the linked list.
     */
    ListElement getNext() {
        return next;
    }

    /**
     * @param next sets the next element of the linked list.
     */
    void setNext(ListElement next) {
        this.next = next;
    }
}