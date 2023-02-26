package muri;

public sealed interface IPAddress extends Host permits IPv4Address, IPv6Address, IPvFutureAddress {
	@Override String toString();
}
