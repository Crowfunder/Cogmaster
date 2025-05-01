namespace Cogmaster.Src.Helpers;

public interface IFileReader
{
    Task<T> ReadAsync<T>(string filePath);
}
