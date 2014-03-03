package org.ohmage.bin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * A {@link MultiValueResult} which is the product of aggregating multiple
 * {@link Collection} and/or {@link MultiValueResult} objects.
 * </p>
 *
 * @author John Jenkins
 */
public class MultiValueResultAggregation<T> implements MultiValueResult<T> {
    /**
     * <p>
     * The aggregator for the {@link MultiValueResultAggregation}. This class
     * should be used to aggregate the data and its {@link #build(long, long)}
     * function should be used to produce a {@link MultiValueResult} object.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Aggregator<T extends Comparable<? super T>> {
        /**
         * The current list of results.
         */
        private final List<T> results = new LinkedList<T>();
        /**
         * The current count of total elements.
         */
        private long count = 0;

        /**
         * Creates a new aggregator with no base data.
         */
        public Aggregator() {
            // Do nothing.
        }

        /**
         * Creates a new aggregator using the given {@link Collection} as a
         * base.
         *
         * @param base
         *        The {@link Collection} to use as the base for this
         *        aggregator.
         */
        public Aggregator(final Collection<T> base) {
            results.addAll(base);
            count = base.size();
        }

        /**
         * Creates a new aggregator using the given {@link MultiValueResult} as
         * a base.
         *
         * @param base
         *        The {@link MultiValueResult} to use as the base for this
         *        aggregator.
         */
        public Aggregator(final MultiValueResult<T> base) {
            for(T object : base) {
                results.add(object);
            }
            count = base.count();
        }

        /**
         * Adds the elements of a {@link Collection} to this aggregator.
         *
         * @param input
         *        The {@link Collection} whose elements should be added to this
         *        aggregator.
         *
         * @return This aggregator to facilitate chaining.
         */
        public Aggregator<T> add(final Collection<T> input) {
            results.addAll(input);
            count += input.size();

            return this;
        }

        /**
         * Adds the elements of a {@link Collection} to this aggregator.
         *
         * @param input
         *        The {@link Collection} whose elements should be added to this
         *        aggregator.
         *
         * @param count
         *        The amount to increase the count by.
         *
         * @return This aggregator to facilitate chaining.
         */
        public Aggregator<T> add(final Collection<T> input, final long count) {
            results.addAll(input);
            this.count += count;

            return this;
        }

        /**
         * Adds the elements of {@link MultiValueResult} to this aggregator.
         *
         * @param input
         *        The {@link MultiValueResult} whose elements should be added
         *        to this aggregator.
         *
         * @return This aggregator to facilitate chaining.
         */
        public Aggregator<T> add(final MultiValueResult<T> input) {
            for(T object : input) {
                results.add(object);
            }
            count += input.count();

            return this;
        }

        /**
         * Builds a {@link MultiValueResult} based on the current state of this
         * aggregator.
         *
         * @param numToSkip
         *        The number of elements to remove from the head of the list
         *        after it is sorted.
         *
         * @param numToReturn
         *        The maximum number of elements to retain in the list after
         *        the parsing has been completed.
         *
         * @return A {@link MultiValueResultAggregation} based on the current
         *         state of this aggregator.
         */
        public MultiValueResultAggregation<T> build(
            final long numToSkip,
            final long numToReturn) {

            // Sort the list.
            Collections.sort(results);

            // Build the result.
            List<T> result = new ArrayList<T>((int) numToReturn);
            if(numToSkip > results.size()) {
                result = Collections.emptyList();
            }
            else {
                // Create an iterator to use to copy the data.
                Iterator<T> iter = results.iterator();

                // Skip the undesired elements.
                for(int i = 0; i < numToSkip; i++) {
                    iter.next();
                }

                // Add the desired elements.
                for(int i = 0; (i < numToReturn) && (iter.hasNext()); i++) {
                    result.add(iter.next());
                }
            }

            // Build the result from a sub-set of the list.
            return new MultiValueResultAggregation<T>(result, count);
        }
    }

    /**
     * The list that backs this collection.
     */
    private final List<T> results;
    /**
     * The overall number of elements that match the given query regardless of
     * the number of elements that are actually being returned.
     */
    private final long count;

    /**
     * Builds a new MultiValueResultAggregation object from a base collection
     * and with a specific count.
     *
     * @param base
     *        The collection of objects that back this object.
     *
     * @param count
     *        The total count, which may be larger than the given collection's
     *        size.
     *
     * @throws IllegalArgumentException
     *         The base collection is null.
     */
    public MultiValueResultAggregation(
        final Collection<T> base,
        final long count)
        throws IllegalArgumentException {

        if(base == null) {
            throw new IllegalArgumentException("The base is null.");
        }

        results = new ArrayList<T>(base);
        this.count = count;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        return results.iterator();
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.MultiValueResult#size()
     */
    @Override
    public long size() {
        return results.size();
    }

    /*
     * (non-Javadoc)
     * @see org.ohmage.bin.MultiValueResult#count()
     */
    @Override
    public long count() {
        return count;
    }
}