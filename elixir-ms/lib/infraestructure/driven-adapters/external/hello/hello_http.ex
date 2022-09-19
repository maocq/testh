defmodule ElixirMs.Adapters.HelloHttp do
  alias ElixirMs.Adapters.Http.HttpClient
  alias ElixirMs.DrivenAdapters.Secrets.SecretManagerAdapter

  @behaviour ElixirMs.Model.Behaviour.HelloRepository

  def hello(latency) do
    %{ external_service_ip: external_service_ip } = SecretManagerAdapter.get_secret()

    case HttpClient.request(:https, "n1.apidevopsteam.xyz", 443, "GET", "/#{latency}") do
      {:ok, response} ->
        {:ok, List.first(response.data)}
      error -> error
    end
  end

  def hello_connection_pool(latency) do
    %{ external_service_ip: external_service_ip } = SecretManagerAdapter.get_secret()
    url = "https://n1.apidevopsteam.xyz/#{latency}"

    case Finch.build(:get, url) |> Finch.request(HttpFinch, [pool_timeout: 30_000]) do
      {:ok, %Finch.Response{body: body}} -> {:ok, body}
      error -> error
    end
  end
end
