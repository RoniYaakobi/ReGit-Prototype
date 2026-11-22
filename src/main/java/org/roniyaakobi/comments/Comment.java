package org.roniyaakobi.comments;

/**
 * A comment on a range of lines in a file. The line range can change as the file is edited
 */
public class Comment {
    /**
     * Separates the fields of a comment record in the comments file
     */
    public static final String FIELD_DELIMITER = "\u001F";

    /**
     * Separates comment records in the comments file
     */
    public static final String RECORD_DELIMITER = "\u001E";

    private final String content;
    private int startLine;
    private int endLine;

    public Comment(String content, int startLine, int endLine){
        this.content = content;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    /**
     * Get the text of the comment
     * @return the content
     */
    public String getContent(){
        return content;
    }

    /**
     * Get the first line the comment is on
     * @return the start line
     */
    public int getStartLine(){
        return startLine;
    }

    /**
     * Get the last line the comment is on
     * @return the end line
     */
    public int getEndLine(){
        return endLine;
    }

    /**
     * Set the first line the comment is on
     * @param startLine the new start line
     */
    public void setStartLine(int startLine){
        this.startLine = startLine;
    }

    /**
     * Set the last line the comment is on
     * @param endLine the new end line
     */
    public void setEndLine(int endLine){
        this.endLine = endLine;
    }

    @Override
    public String toString(){
        return "Comment[lines=" + startLine + "-" + endLine + ",content=" + content + "]";
    }
}
