package muri;

public sealed interface Host permits IPAddress, RegisteredName {
	@Override String toString();
}
