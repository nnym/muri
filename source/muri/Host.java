package muri;

public sealed interface Host permits IPAddress, Name {
	@Override String toString();
}
