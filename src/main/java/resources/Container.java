package resources;

public abstract class Container<I extends Item> {
	public abstract void create(final I item);
	public abstract I read(final String _id);
	public abstract I update(final I item);
	public abstract void delete(final String _id);
}
