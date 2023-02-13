package it.unipd.dei.conversation;

import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 * The {@code Conversation} class is a container for all {@link Utterance}s belonging to the same conversation.
 *
 * @author Marco Alessio
 */
@SuppressWarnings("unused")
public final class Conversation implements Iterable<Utterance>
{
    private final String id;
    private final List<Utterance> utterancesList;
    private final Map<String, Utterance> utterancesMap;


    /**
     * Create a new conversation, with the given ID.
     *
     * @param id The ID of the conversation.
     * @throws NullPointerException If the provided ID is null.
     */
    public Conversation(String id)
    {
        if (id == null)
            throw new NullPointerException("The provided ID is null.");

        this.id = id;
        utterancesList = new ArrayList<>();
        utterancesMap = new TreeMap<>();
    }


    /**
     * Add an utterance to this conversation.
     *
     * @param utterance The utterance to add.
     * @throws NullPointerException If the provided utterance is null.
     * @throws RuntimeException If the provided utterance has an ID that is already used by another utterance
     * in this conversation.
     */
    public void addUtterance(Utterance utterance)
    {
        if (utterance == null)
            throw new NullPointerException("The provided utterance is null.");

        if (utterancesMap.containsKey(utterance.getID()))
        {
            throw new RuntimeException(String.format("The provided utterance has an ID (%s) that is already used " +
                    "by another utterance in this conversation.", utterance.getID()));
        }

        utterancesList.add(utterance);
        utterancesMap.put(utterance.getID(), utterance);
    }


    /**
     * Add an utterance to this conversation, inserting it in a specific position.
     *
     * @param utterance The utterance to add.
     * @param turn The position in the conversation where to add the utterance.
     * @throws NullPointerException If the provided utterance is null.
     * @throws IllegalArgumentException If the provided turn is a negative integer or is greater or equal to
     * conversation length.
     * @throws RuntimeException If the provided utterance has an ID that is already used by another utterance
     * in this conversation.
     */
    public void insertUtterance(Utterance utterance, int turn)
    {
        if (utterance == null)
            throw new NullPointerException("The provided utterance is null.");

        if (turn < 0)
        {
            throw new IllegalArgumentException(String.format("The provided turn (%d) must be a positive integer " +
                    "number.", turn));
        }

        if (turn >= utterancesList.size())
        {
            throw new IllegalArgumentException(String.format("The provided turn (%d) is greater or equal to " +
                    "conversation length (%d).", turn, utterancesList.size()));
        }

        if (utterancesMap.containsKey(utterance.getID()))
        {
            throw new RuntimeException(String.format("The provided utterance has an ID (%s) that is already used " +
                    "in this conversation.", utterance.getID()));
        }

        utterancesList.add(turn, utterance);
        utterancesMap.put(utterance.getID(), utterance);
    }


    /**
     * Return the ID of this conversation.
     *
     * @return The ID of this conversation.
     */
    public String getID()
    {
        return id;
    }


    /**
     * Return the utterance with the given ID, or {@code null} if this conversation does not contain an utterance
     * with ID equal to the provided one.
     *
     * @param id The ID of the utterance.
     * @throws NullPointerException If the provided ID is null.
     * @return The utterance with ID equal to the provided one, if exists, otherwise {@code null}.
     */
    public Utterance getUtteranceByID(String id)
    {
        if (id == null)
            throw new NullPointerException("The provided ID is null.");

        return utterancesMap.get(id);
    }


    /**
     * Return the utterance at the given position.
     *
     * @param turn The position in the conversation where the utterance is stored.
     * @throws IllegalArgumentException If the provided turn is a negative integer or is greater or equal to
     * conversation length.
     * @return The utterance at the given position.
     */
    public Utterance getUtteranceByTurn(int turn)
    {
        if (turn < 0)
        {
            throw new IllegalArgumentException(String.format("The provided turn (%d) must be a positive integer " +
                    "number.", turn));
        }

        if (turn >= utterancesList.size())
        {
            throw new IllegalArgumentException(String.format("The provided turn (%d) is greater or equal to " +
                    "conversation length (%d).", turn, utterancesList.size()));
        }

        return utterancesList.get(turn);
    }


    /**
     * Return an {@link Iterator} over all {@link Utterance}s of this conversation.
     *
     * @return The iterator over all utterances.
     */
    @NotNull
    @Override
    public Iterator<Utterance> iterator()
    {
        return utterancesList.iterator();
    }


    /**
     * Return the number of utterances stored inside this conversation.
     *
     * @return The number of utterances stored inside this conversation.
     */
    public int size()
    {
        return utterancesList.size();
    }
}
