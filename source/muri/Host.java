package muri;

public sealed interface Host permits IPAddress, Name {
	@Override int hashCode();

	@Override boolean equals(Object o);

	@Override String toString();
}
