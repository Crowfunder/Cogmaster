using System.Text.Json;

namespace Cogmaster.Src.Helpers;

public class JsonFileReader : IFileReader
{
    public async Task<T> ReadAsync<T>(string filePath)
    {
        using FileStream stream = File.OpenRead("Src/" + filePath);
        return await JsonSerializer.DeserializeAsync<T>(stream) ?? throw new FileNotFoundException();
    }
}
